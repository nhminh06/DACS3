console.log("Admin Common JS v2.0 Initializing...");

function showLoadingOverlay(text = "Đang xử lý...") {
    const overlay = document.getElementById('loadingOverlay');
    const progressBar = document.getElementById('progressBar');
    const loadingLabel = overlay.querySelector('.loading-text');

    if (overlay) {
        loadingLabel.innerText = text;
        overlay.classList.add('show'); // Sử dụng class thay vì style.display

        let width = 5; // Bắt đầu ở 5% để người dùng thấy ngay thanh đang chạy
        progressBar.style.width = width + '%';

        const interval = setInterval(function() {
            if (width >= 98) {
                clearInterval(interval);
            } else {
                // Chạy nhanh lúc đầu, chậm dần về sau (fake progress)
                let increment = (100 - width) * 0.1;
                width += increment;
                if(progressBar) progressBar.style.width = width + '%';
            }
        }, 400);
    }
}

document.addEventListener('DOMContentLoaded', function() {
    // 1. Tự động chèn HTML Loading nếu chưa có
    if (!document.getElementById('loadingOverlay')) {
        const overlayHtml = `
            <div id="loadingOverlay" class="loading-overlay">
                <div class="loading-box">
                    <div class="spinner"></div>
                    <div class="loading-text">Đang xử lý...</div>
                    <div class="progress-container">
                        <div id="progressBar" class="progress-bar"></div>
                    </div>
                    <div class="mt-2 text-muted small">Vui lòng chờ trong giây lát</div>
                </div>
            </div>`;
        document.body.insertAdjacentHTML('beforeend', overlayHtml);
    }

    // 2. Bắt sự kiện submit cho tất cả các form (kể cả form thêm tour)
    document.addEventListener('submit', function(event) {
        const form = event.target;
        if (form.tagName === 'FORM' && !form.classList.contains('no-loading')) {
            console.log("Form submission detected, showing loading...");
            showLoadingOverlay("Đang lưu dữ liệu...");
        }
    }, true);

    // 3. Bắt sự kiện click cho các link hành động
    document.addEventListener('click', function(event) {
        const link = event.target.closest('a');
        if (!link) return;

        const href = link.getAttribute('href');
        if (href && (href.includes('delete') || href.includes('toggle') || href.includes('confirm') || href.includes('cancel'))) {
            // Nếu có confirm() thì trình duyệt sẽ dừng link nếu nhấn Cancel.
            // Chúng ta show loading sau một khoảng trễ cực ngắn
            setTimeout(() => {
                // Kiểm tra xem link có thực sự đang điều hướng không
                showLoadingOverlay("Đang thực hiện...");
            }, 50);
        }
    }, true);
});
