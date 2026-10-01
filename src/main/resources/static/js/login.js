document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const btnLogin = document.getElementById('btnLogin');
    const btnText = document.getElementById('btnText');
    const btnLoader = document.getElementById('btnLoader');
    const alertBox = document.getElementById('alertBox');

    // Si ya tiene token válido, verificar y redirigir
    const token = localStorage.getItem('auth_token');
    if (token) {
        fetch('/api/auth/check', {
            headers: { 'Authorization': 'Bearer ' + token }
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                window.location.href = 'admin-tabla.html';
            }
        })
        .catch(() => localStorage.removeItem('auth_token'));
    }

    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        showAlert('', false);
        setLoading(true);

        const payload = {
            username: usernameInput.value.trim(),
            password: passwordInput.value.trim()
        };

        try {
            const res = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            const data = await res.json();

            if (!res.ok || !data.success) {
                throw new Error(data.message || 'Usuario o contraseña incorrectos');
            }

            // Guardar token y datos de sesión
            localStorage.setItem('auth_token', data.token);
            localStorage.setItem('auth_user', data.username);
            localStorage.setItem('auth_name', data.nombreCompleto);
            localStorage.setItem('auth_role', data.rol);

            showAlert('Autenticado con éxito. Redirigiendo...', false);

            setTimeout(() => {
                window.location.href = 'admin-tabla.html';
            }, 600);

        } catch (err) {
            showAlert(err.message, true);
        } finally {
            setLoading(false);
        }
    });

    function showAlert(msg, isError) {
        if (!msg) {
            alertBox.classList.add('hidden');
            return;
        }
        alertBox.textContent = msg;
        alertBox.className = isError
            ? 'mb-5 p-3.5 rounded-xl text-xs font-medium bg-red-500/20 text-red-300 border border-red-500/30'
            : 'mb-5 p-3.5 rounded-xl text-xs font-medium bg-green-500/20 text-green-300 border border-green-500/30';
        alertBox.classList.remove('hidden');
    }

    function setLoading(isLoading) {
        btnLogin.disabled = isLoading;
        if (isLoading) {
            btnText.textContent = 'Verificando...';
            btnLoader.classList.remove('hidden');
        } else {
            btnText.textContent = 'Ingresar al Sistema';
            btnLoader.classList.add('hidden');
        }
    }
});
