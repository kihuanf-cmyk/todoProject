document.addEventListener('DOMContentLoaded', function () {
  const form = document.getElementById('joinForm');
  if (!form) return;

  form.addEventListener('submit', function (event) {
    const userId = form.querySelector('input[name="user_id"]').value.trim();
    if (!userId) {
      event.preventDefault();
      form.querySelector('input[name="user_id"]').focus();
    }
  });
});
