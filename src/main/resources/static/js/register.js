document.addEventListener('DOMContentLoaded', function () {
  const input = document.getElementById('fileInput');
  const fileName = document.getElementById('fileName');
  const previewSection = document.getElementById('previewSection');
  const imagePreview = document.getElementById('imagePreview');
  const clearButton = document.getElementById('clearFileButton');

  if (!input) return;

  function clearSelectedFile() {
    input.value = '';
    fileName.textContent = '';
    imagePreview.src = '';
    previewSection.style.display = 'none';
  }

  clearButton.addEventListener('click', clearSelectedFile);

  input.addEventListener('change', function () {
    const file = input.files[0];

    if (!file) {
      clearSelectedFile();
      return;
    }

    if (file.size > 10 * 1024 * 1024) {
      alert('파일은 10MB 이하만 업로드할 수 있습니다.');
      clearSelectedFile();
      return;
    }

    fileName.textContent = file.name;

    if (file.type.startsWith('image/')) {
      const reader = new FileReader();
      reader.onload = function (event) {
        imagePreview.src = event.target.result;
        previewSection.style.display = 'block';
      };
      reader.readAsDataURL(file);
    } else {
      imagePreview.src = '';
      previewSection.style.display = 'none';
    }
  });
});
