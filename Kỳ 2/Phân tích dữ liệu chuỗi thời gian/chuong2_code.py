import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

# 1. Định nghĩa dữ liệu
datasets = {
    "Tập 1": [10, 12, 11, 13, 14, 12, 15],
    "Tập 2": [11, 13, 10, 14, 15, 11, 16],
    "Tập 3": [12, 11, 13, 10, 14, 15, 16]
}

# 2. Tham số cấu hình
k_window = 3
alpha_ema = 0.5

# 3. Hàm tính toán
def calculate_smoothing(data, k, alpha):
    n = len(data)
    ma = [np.nan] * n
    wma = [np.nan] * n
    ema = [np.nan] * n
    
    # Sinh trọng số WMA: [1, 2, ..., k]
    weights = np.arange(1, k + 1)
    sum_weights = np.sum(weights)

    for i in range(n):
        # Tính MA và WMA
        if i >= k - 1:
            window = data[i - k + 1 : i + 1]
            ma[i] = np.mean(window)
            wma[i] = np.dot(window, weights) / sum_weights
            
        # Tính EMA
        if i == 0:
            ema[i] = data[i]
        else:
            ema[i] = alpha * data[i] + (1 - alpha) * ema[i - 1]
            
    return ma, wma, ema

# 4. Tính toán, In kết quả và Chuẩn bị vẽ đồ thị
fig, axes = plt.subplots(3, 1, figsize=(10, 18))
plt.subplots_adjust(hspace=0.4)

for idx, (name, data) in enumerate(datasets.items()):
    ma_vals, wma_vals, ema_vals = calculate_smoothing(data, k_window, alpha_ema)
    
    # Tạo bảng hiển thị
    df = pd.DataFrame({
        't': range(1, len(data) + 1),
        'Gốc': data,
        'MA': ma_vals,
        'WMA': wma_vals,
        'EMA': ema_vals
    })
    
    print(f"\n{'='*10} KẾT QUẢ: {name} {'='*10}")
    # Làm tròn 2 chữ số và thay NaN bằng '-'
    print(df.round(2).fillna('-').to_string(index=False))
    
    # Vẽ đồ thị
    ax = axes[idx]
    t_axis = df['t']
    ax.plot(t_axis, df['Gốc'], marker='o', linestyle='--', color='black', label='Gốc', alpha=0.7)
    ax.plot(t_axis, df['MA'], marker='s', color='blue', label=f'MA (k={k_window})')
    ax.plot(t_axis, df['WMA'], marker='^', color='green', label=f'WMA (k={k_window})')
    ax.plot(t_axis, df['EMA'], marker='D', color='red', label=f'EMA (alpha={alpha_ema})')
    
    ax.set_title(f'So sánh các phương pháp làm mịn - {name}')
    ax.set_xlabel('Thời gian (t)')
    ax.set_ylabel('Giá trị')
    ax.legend()
    ax.grid(True, linestyle=':', alpha=0.7)

plt.show()