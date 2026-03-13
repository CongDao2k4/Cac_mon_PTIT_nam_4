import os
import re
import zipfile
from collections import defaultdict
import pandas as pd

# --- CẤU HÌNH ---
CRANFIELD_ZIP = "Cranfield.zip"
TEST_CSV = "test.csv"
TOP_K = 50
TOKEN_PATTERN = re.compile(r"[a-z0-9]+")

# Danh sách từ dừng cơ bản (Stopwords) để lọc nhiễu
STOPWORDS = {
    "a", "an", "the", "and", "or", "but", "if", "because", "as", "what",
    "when", "where", "how", "who", "which", "this", "that", "these", "those",
    "is", "are", "was", "were", "be", "been", "being", "have", "has", "had",
    "do", "does", "did", "at", "by", "for", "from", "in", "into", "of", "off",
    "on", "onto", "out", "over", "up", "with", "to", "about", "can", "could",
    "will", "would", "should", "must", "may", "might"
}

def tokenize(text: str):
    if not isinstance(text, str): return []
    words = TOKEN_PATTERN.findall(text.lower())
    # Lọc bỏ stopwords để tập trung vào từ khóa chính
    return [w for w in words if w not in STOPWORDS]

def load_documents(zip_path):
    documents = {}
    if not os.path.exists(zip_path):
        print(f"Lỗi: Không tìm thấy file {zip_path}")
        return {}
        
    with zipfile.ZipFile(zip_path, "r") as z:
        for name in z.namelist():
            name = name.replace("\\", "/")
            if name.startswith("Cranfield/") and name.endswith(".txt"):
                try:
                    doc_id = int(os.path.splitext(os.path.basename(name))[0])
                    with z.open(name) as f:
                        documents[doc_id] = f.read().decode("utf-8", errors="ignore")
                except ValueError:
                    continue
    print(f"Đã tải {len(documents)} tài liệu.")
    return documents

# ==========================================
# PHƯƠNG PHÁP 1: CHỈ MỤC NGƯỢC (BASELINE)
# ==========================================
def build_inverted_index(documents):
    inverted_index = defaultdict(set)
    doc_terms = {}
    for doc_id, text in documents.items():
        terms = tokenize(text)
        doc_terms[doc_id] = set(terms)
        for term in terms:
            inverted_index[term].add(doc_id)
    return inverted_index, doc_terms

def retrieve_inverted(query, inverted_index, doc_terms):
    q_terms = tokenize(query)
    candidate_docs = set()
    for term in q_terms:
        candidate_docs |= inverted_index.get(term, set())

    scored = []
    for doc_id in candidate_docs:
        # Điểm = Số từ khớp (không xét thứ tự)
        score = sum(1 for t in q_terms if t in doc_terms[doc_id])
        scored.append((score, doc_id))

    # Sắp xếp: Điểm cao -> DocID nhỏ
    scored.sort(key=lambda x: (-x[0], x[1]))
    return [doc_id for _, doc_id in scored[:TOP_K]]

# ==========================================
# PHƯƠNG PHÁP 2: CHỈ MỤC VỊ TRÍ (POSITIONAL) - NÂNG CAO
# ==========================================
def build_positional_index(documents):
    pos_index = defaultdict(lambda: defaultdict(list))
    for doc_id, text in documents.items():
        # Ở đây KHÔNG lọc stopwords khi đánh chỉ mục để giữ đúng vị trí
        # Nhưng khi truy vấn sẽ chỉ quan tâm từ khóa chính
        raw_terms = TOKEN_PATTERN.findall(text.lower())
        for pos, term in enumerate(raw_terms):
            pos_index[term][doc_id].append(pos)
    return pos_index

def retrieve_positional(query, pos_index):
    # Tokenize truy vấn có lọc stopword
    q_terms = tokenize(query)
    if not q_terms: return []
    
    # Tìm tập tài liệu chứa ít nhất 1 từ khóa
    candidate_docs = set()
    for term in q_terms:
        candidate_docs.update(pos_index.get(term, {}).keys())
    
    scored = []
    for doc_id in candidate_docs:
        match_count = 0     # Số lượng từ khóa khác nhau có trong doc
        phrase_score = 0    # Điểm cụm từ
        term_freq = 0       # Tổng số lần xuất hiện
        
        # 1. Tính match_count và term_freq
        matches = []
        for term in q_terms:
            positions = pos_index.get(term, {}).get(doc_id, [])
            if positions:
                match_count += 1
                term_freq += len(positions)
                matches.append((term, positions))
        
        # 2. Tính phrase_score (Cụm từ liền nhau)
        # So sánh các cặp từ khóa đứng cạnh nhau trong truy vấn
        # Ví dụ query: "high speed aircraft" -> check "high-speed" và "speed-aircraft"
        for i in range(len(q_terms) - 1):
            t1, t2 = q_terms[i], q_terms[i+1]
            if t1 in pos_index and t2 in pos_index:
                pos1_list = pos_index[t1].get(doc_id, [])
                pos2_list = pos_index[t2].get(doc_id, [])
                
                # Kiểm tra xem có vị trí nào liền kề không (p2 = p1 + 1)
                # Hoặc gần nhau (p2 - p1 <= 3)
                for p1 in pos1_list:
                    for p2 in pos2_list:
                        if 0 < p2 - p1 <= 2: # Cho phép cách nhau 1 từ đệm
                            phrase_score += 1
                            break # Tính 1 lần cho cặp này để tránh loop nhiều
                            
        # 3. TÍNH ĐIỂM TỔNG HỢP (Tiered Scoring)
        # Công thức: (Số từ khớp * 10000) + (Điểm cụm từ * 100) + (Tần suất)
        # Mục đích: Bắt buộc tài liệu phải chứa nhiều từ khóa nhất lên đầu
        final_score = (match_count * 10000) + (phrase_score * 100) + term_freq
        
        scored.append((final_score, doc_id))
    
    scored.sort(key=lambda x: (-x[0], x[1]))
    return [doc_id for _, doc_id in scored[:TOP_K]]

# ==========================================
# MAIN EXECUTION
# ==========================================
def run_tests():
    if not os.path.exists(TEST_CSV):
        print(f"LỖI: Không tìm thấy file '{TEST_CSV}'.")
        return

    documents = load_documents(CRANFIELD_ZIP)
    if not documents: return

    queries = pd.read_csv(TEST_CSV)
    
    # --- CHẠY BASELINE ---
    print("Đang chạy phương pháp Chỉ mục ngược (Baseline)...")
    inv_index, doc_terms = build_inverted_index(documents)
    results_inv = []
    for _, row in queries.iterrows():
        relevant_docs = retrieve_inverted(row["query"], inv_index, doc_terms)
        results_inv.append({
            "query_id": row["query_id"],
            "query": row["query"],
            "relevant_docs": " ".join(str(d) for d in relevant_docs)
        })
    pd.DataFrame(results_inv)[["query_id", "query", "relevant_docs"]].to_csv("submission_inverted.csv", index=False)
    print("-> Đã xuất file submission_inverted.csv")

    # --- CHẠY POSITIONAL ---
    print("Đang chạy phương pháp Chỉ mục vị trí (Positional - Optimized)...")
    pos_index = build_positional_index(documents)
    results_pos = []
    for _, row in queries.iterrows():
        relevant_docs = retrieve_positional(row["query"], pos_index)
        results_pos.append({
            "query_id": row["query_id"],
            "query": row["query"],
            "relevant_docs": " ".join(str(d) for d in relevant_docs)
        })
    pd.DataFrame(results_pos)[["query_id", "query", "relevant_docs"]].to_csv("submission_positional.csv", index=False)
    print("-> Đã xuất file submission_positional.csv")

if __name__ == "__main__":
    run_tests()