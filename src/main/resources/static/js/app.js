const API = "/api";
let currentUser = JSON.parse(localStorage.getItem("communityStoreUser") || "null");
let cart = JSON.parse(localStorage.getItem("communityStoreCart") || "[]");

const $ = (id) => document.getElementById(id);

function showToast(message) {
    const toast = $("toast");
    toast.textContent = message;
    toast.classList.remove("hidden");
    setTimeout(() => toast.classList.add("hidden"), 2600);
}

function updateUserUI() {
    $("welcomeText").textContent = currentUser ? `Hi, ${currentUser.fullName}` : "Guest";
    $("loginBtn").classList.toggle("hidden", !!currentUser);
    $("logoutBtn").classList.toggle("hidden", !currentUser);
}

function setView(viewId) {
    document.querySelectorAll(".view").forEach(v => v.classList.remove("active"));
    $(viewId).classList.add("active");

    if (viewId === "cart") renderCart();
    if (viewId === "bulletin") loadBulletin();
    if (viewId === "notifications") loadNotifications();
}

document.querySelectorAll(".nav button").forEach(button => {
    button.addEventListener("click", () => setView(button.dataset.view));
});

$("loginBtn").addEventListener("click", () => $("authModal").classList.remove("hidden"));
$("closeModal").addEventListener("click", () => $("authModal").classList.add("hidden"));

$("logoutBtn").addEventListener("click", () => {
    currentUser = null;
    localStorage.removeItem("communityStoreUser");
    updateUserUI();
    showToast("Logged out");
});

$("showLogin").addEventListener("click", () => {
    $("loginForm").classList.remove("hidden");
    $("registerForm").classList.add("hidden");
    $("showLogin").classList.add("active");
    $("showRegister").classList.remove("active");
});

$("showRegister").addEventListener("click", () => {
    $("registerForm").classList.remove("hidden");
    $("loginForm").classList.add("hidden");
    $("showRegister").classList.add("active");
    $("showLogin").classList.remove("active");
});

$("registerForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    const body = {
        fullName: $("registerName").value,
        email: $("registerEmail").value,
        contactNumber: $("registerContact").value,
        password: $("registerPassword").value
    };

    const response = await fetch(`${API}/auth/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
    });

    const data = await response.json();

    if (!response.ok) {
        $("authMessage").textContent = data.message || "Registration failed";
        return;
    }

    currentUser = data;
    localStorage.setItem("communityStoreUser", JSON.stringify(currentUser));
    $("authModal").classList.add("hidden");
    updateUserUI();
    showToast("Account created successfully");
});

$("loginForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    const response = await fetch(`${API}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            email: $("loginEmail").value,
            password: $("loginPassword").value
        })
    });

    const data = await response.json();

    if (!response.ok) {
        $("authMessage").textContent = data.message || "Login failed";
        return;
    }

    currentUser = data;
    localStorage.setItem("communityStoreUser", JSON.stringify(currentUser));
    $("authModal").classList.add("hidden");
    updateUserUI();
    showToast("Logged in successfully");
});

async function loadProducts() {
    const params = new URLSearchParams();

    if ($("searchInput").value) params.set("q", $("searchInput").value);
    if ($("categoryFilter").value) params.set("category", $("categoryFilter").value);
    if ($("locationFilter").value) params.set("location", $("locationFilter").value);
    if ($("minPrice").value) params.set("minPrice", $("minPrice").value);
    if ($("maxPrice").value) params.set("maxPrice", $("maxPrice").value);

    const response = await fetch(`${API}/products?${params}`);
    const products = await response.json();

    const grid = $("productsGrid");

    if (products.length === 0) {
        grid.innerHTML = `<div class="panel"><p>No products found yet.</p></div>`;
        return;
    }

    grid.innerHTML = products.map(product => `
        <article class="card">
            <img class="product-image"
                 src="${product.imageUrl || 'https://placehold.co/600x400?text=Community+Store'}"
                 alt="${escapeHtml(product.title)}">
            <h3>${escapeHtml(product.title)}</h3>
            <div class="price">R${Number(product.price).toFixed(2)}</div>
            <p>${escapeHtml(product.description)}</p>
            <p class="muted">${escapeHtml(product.category || "Other")} • ${escapeHtml(product.location || "Campus")}</p>
            <p class="muted">Seller: ${escapeHtml(product.sellerName || "Community member")}</p>
            <button onclick='addToCart(${JSON.stringify(product)})'>Add to Cart</button>
        </article>
    `).join("");
}

$("searchBtn").addEventListener("click", loadProducts);

$("productForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    if (!currentUser) {
        $("authModal").classList.remove("hidden");
        showToast("Please log in before creating a listing");
        return;
    }

    const body = {
        title: $("productTitle").value,
        description: $("productDescription").value,
        price: Number($("productPrice").value),
        category: $("productCategory").value,
        location: $("productLocation").value,
        imageUrl: $("productImage").value,
        sellerId: currentUser.id,
        sellerName: currentUser.fullName
    };

    const response = await fetch(`${API}/products`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
    });

    const data = await response.json();

    if (!response.ok) {
        showToast(data.message || "Could not create listing");
        return;
    }

    event.target.reset();
    showToast("Listing published");
    setView("marketplace");
    loadProducts();
});

function addToCart(product) {
    cart.push(product);
    localStorage.setItem("communityStoreCart", JSON.stringify(cart));
    updateCartCount();
    showToast("Added to cart");
}

window.addToCart = addToCart;

function updateCartCount() {
    $("cartCount").textContent = cart.length;
}

function renderCart() {
    const container = $("cartItems");

    if (cart.length === 0) {
        container.innerHTML = "<p>Your cart is empty.</p>";
        $("cartTotal").textContent = "0.00";
        return;
    }

    container.innerHTML = cart.map((item, index) => `
        <div class="cart-row">
            <div>
                <strong>${escapeHtml(item.title)}</strong>
                <div class="muted">${escapeHtml(item.sellerName || "")}</div>
            </div>
            <div>
                R${Number(item.price).toFixed(2)}
                <button class="secondary" onclick="removeFromCart(${index})">Remove</button>
            </div>
        </div>
    `).join("");

    const total = cart.reduce((sum, item) => sum + Number(item.price), 0);
    $("cartTotal").textContent = total.toFixed(2);
}

function removeFromCart(index) {
    cart.splice(index, 1);
    localStorage.setItem("communityStoreCart", JSON.stringify(cart));
    updateCartCount();
    renderCart();
}

window.removeFromCart = removeFromCart;

$("checkoutBtn").addEventListener("click", async () => {
    if (!currentUser) {
        $("authModal").classList.remove("hidden");
        showToast("Please log in before checkout");
        return;
    }

    if (cart.length === 0) {
        showToast("Your cart is empty");
        return;
    }

    const total = cart.reduce((sum, item) => sum + Number(item.price), 0);

    const response = await fetch(`${API}/checkout`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            userId: currentUser.id,
            total,
            items: cart.map(item => item.id)
        })
    });

    const data = await response.json();

    if (!response.ok) {
        showToast(data.message || "Checkout failed");
        return;
    }

    cart = [];
    localStorage.setItem("communityStoreCart", "[]");
    updateCartCount();
    renderCart();
    showToast("Demo checkout successful");
});

$("bulletinForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    if (!currentUser) {
        $("authModal").classList.remove("hidden");
        showToast("Please log in before posting");
        return;
    }

    const response = await fetch(`${API}/bulletin`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            authorId: currentUser.id,
            authorName: currentUser.fullName,
            type: $("bulletinType").value,
            title: $("bulletinTitle").value,
            content: $("bulletinContent").value
        })
    });

    const data = await response.json();

    if (!response.ok) {
        showToast(data.message || "Could not create post");
        return;
    }

    event.target.reset();
    showToast("Bulletin post created");
    loadBulletin();
});

async function loadBulletin() {
    const response = await fetch(`${API}/bulletin`);
    const posts = await response.json();

    $("bulletinList").innerHTML = posts.length
        ? posts.map(post => `
            <article class="post">
                <div class="muted">${escapeHtml(post.type || "Post")} • ${escapeHtml(post.authorName || "Community member")}</div>
                <h3>${escapeHtml(post.title)}</h3>
                <p>${escapeHtml(post.content)}</p>
            </article>
        `).join("")
        : `<div class="panel"><p>No bulletin posts yet.</p></div>`;
}

async function loadNotifications() {
    if (!currentUser) {
        $("notificationList").innerHTML = "<p>Please log in to see notifications.</p>";
        return;
    }

    const response = await fetch(`${API}/notifications/user/${currentUser.id}`);
    const notifications = await response.json();

    $("notificationList").innerHTML = notifications.length
        ? notifications.map(n => `
            <div class="notification">
                <strong>${n.readStatus ? "Read" : "New"}</strong>
                <p>${escapeHtml(n.message)}</p>
            </div>
        `).join("")
        : "<p>No notifications yet.</p>";
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

updateUserUI();
updateCartCount();
loadProducts();
