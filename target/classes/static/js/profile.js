document.addEventListener("DOMContentLoaded", () => {

    const staticProfile = document.getElementById("staticProfile");
    const editForm = document.getElementById("editForm");

    const editBtn = document.getElementById("editBtn");
    const cancelBtn = document.getElementById("cancelBtn");

    // Show edit mode
    editBtn?.addEventListener("click", () => {
        staticProfile.classList.add("hidden");
        editForm.classList.remove("hidden");
    });

    // Cancel editing
    cancelBtn?.addEventListener("click", () => {
        editForm.classList.add("hidden");
        staticProfile.classList.remove("hidden");
    });

});
