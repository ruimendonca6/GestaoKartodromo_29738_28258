// ── Modal helpers ──────────────────────────────────────
function openModal(id) {
    document.getElementById(id).classList.add('open');
}

function closeModal(id) {
    document.getElementById(id).classList.remove('open');
}

// Close on overlay click
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal-overlay')) {
        e.target.classList.remove('open');
    }
});

// Close on Escape
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        document.querySelectorAll('.modal-overlay.open')
            .forEach(m => m.classList.remove('open'));
    }
});

// ── Populate edit modal from table row ─────────────────
function populateEdit(modalId, data) {
    const modal = document.getElementById(modalId);
    Object.entries(data).forEach(([key, val]) => {
        const el = modal.querySelector('[name="' + key + '"]');
        if (el) el.value = val;
    });
    openModal(modalId);
}

// Called from data-* buttons (Thymeleaf 3.1 restriction on th:onclick strings)
function populateEditFromDataset(btn, modalId) {
    populateEdit(modalId, btn.dataset);
}

// ── Confirm delete ─────────────────────────────────────
function confirmDelete(formId, msg) {
    if (confirm(msg || 'Tem a certeza que quer eliminar?')) {
        document.getElementById(formId).submit();
    }
}

function confirmDeleteEl(btn) {
    var formId = btn.dataset.formId;
    var msg = btn.dataset.msg || 'Tem a certeza que quer eliminar?';
    confirmDelete(formId, msg);
}
