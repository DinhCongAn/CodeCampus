document.addEventListener('DOMContentLoaded', function () {
    const trigger = document.getElementById('profile-trigger');
    const menu = document.getElementById('dropdown-menu');

    if (!trigger || !menu) return;

    let isOpen = false;

    const openMenu = () => {
        if (isOpen) return;
        isOpen = true;
        menu.classList.remove('invisible', 'opacity-0', 'dropdown-enter-from', 'dropdown-leave-to');
        menu.classList.add('opacity-100', 'visible');
        // Trigger reflow để animation chạy
        void menu.offsetWidth;
        menu.classList.add('dropdown-enter-active');
    };

    const closeMenu = () => {
        if (!isOpen) return;
        isOpen = false;
        menu.classList.remove('opacity-100', 'visible', 'dropdown-enter-active');
        menu.classList.add('dropdown-leave-active', 'dropdown-leave-to');
        setTimeout(() => {
            menu.classList.add('invisible', 'opacity-0');
            menu.classList.remove('dropdown-leave-active', 'dropdown-leave-to');
        }, 250);
    };

    // Mở khi click
    trigger.addEventListener('click', function (e) {
        e.stopPropagation();
        isOpen ? closeMenu() : openMenu();
    });

    // Đóng khi click ngoài
    document.addEventListener('click', function (e) {
        if (!trigger.contains(e.target)) {
            closeMenu();
        }
    });

    // Đóng khi nhấn ESC
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && isOpen) {
            closeMenu();
        }
    });
});