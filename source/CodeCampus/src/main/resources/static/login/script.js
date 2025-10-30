// === Creative Login Script (clean version) ===
document.addEventListener("DOMContentLoaded", () => {
    // Elements
    const passwordInput = document.getElementById("password");
    const passwordToggle = document.getElementById("passwordToggle");
    const emailInput = document.getElementById("email");
    const rememberCheckbox = document.getElementById("remember");
    const form = document.getElementById("loginForm");

    // === ẨN / HIỆN MẬT KHẨU ===
    if (passwordToggle && passwordInput) {
        passwordToggle.addEventListener("click", () => {
            const type = passwordInput.type === "password" ? "text" : "password";
            passwordInput.type = type;
            passwordToggle.classList.toggle("active");
        });
    }

    // === GHI NHỚ EMAIL ===
    const savedEmail = localStorage.getItem("creativeRememberedEmail");
    if (savedEmail) {
        emailInput.value = savedEmail;
        rememberCheckbox.checked = true;
    }

    // Khi submit form
    if (form) {
        form.addEventListener("submit", () => {
            if (rememberCheckbox.checked) {
                localStorage.setItem("creativeRememberedEmail", emailInput.value);
            } else {
                localStorage.removeItem("creativeRememberedEmail");
            }
        });
    }
});
