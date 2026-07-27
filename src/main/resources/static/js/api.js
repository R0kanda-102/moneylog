const API_BASE = "";

async function apiFetch(path, options = {}) {
    const token = localStorage.getItem("accessToken");
    const headers = {
        "Content-Type": "application/json",
        ...(options.headers || {}),
    };
    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }

    const response = await fetch(API_BASE + path, { ...options, headers });

    if (response.status === 401) {
        localStorage.removeItem("accessToken");
        if (!location.pathname.endsWith("login.html")) {
            alert("로그인이 필요합니다.");
            location.href = "/login.html";
        }
        throw new Error("UNAUTHORIZED");
    }

    const body = await response.json();

    if (!body.success) {
        throw new Error(body.message || "요청 처리 중 오류가 발생했습니다.");
    }

    return body;
}