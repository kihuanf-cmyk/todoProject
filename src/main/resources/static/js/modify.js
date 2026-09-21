document.addEventListener('DOMContentLoaded', function () {
  const input = document.getElementById('fileInput');
  const fileName = document.getElementById('fileName');
  const currentFileSection = document.getElementById('currentFileSection');
  const deleteFileInput = document.getElementById('deleteFile');
  const deleteFileButton = document.getElementById('deleteFileButton');
  const cancelNewFileButton = document.getElementById('cancelNewFileButton');
  const previewSection = document.getElementById('newFilePreviewSection');
  const previewImage = document.getElementById('newImagePreview');
  const deleteTodoButton = document.getElementById('deleteTodoButton');
  const deleteForm = document.getElementById('deleteForm');

  if (!input) return;

  function cancelNewFile() {
    input.value = '';
    fileName.textContent = '';
    previewSection.style.display = 'none';
    previewImage.src = '';
  }

  if (deleteFileButton) {
    deleteFileButton.addEventListener('click', function () {
      deleteFileInput.value = 'true';
      if (currentFileSection) currentFileSection.style.display = 'none';
    });
  }

  if (cancelNewFileButton) {
    cancelNewFileButton.addEventListener('click', cancelNewFile);
  }

  if (deleteTodoButton && deleteForm) {
    deleteTodoButton.addEventListener('click', function () {
      if (confirm('일정을 삭제하시겠습니까?')) {
        deleteForm.submit();
      }
    });
  }

  input.addEventListener('change', function () {
    const file = input.files[0];

    if (!file) {
      cancelNewFile();
      return;
    }

    if (file.size > 10 * 1024 * 1024) {
      alert('파일은 10MB 이하만 업로드할 수 있습니다.');
      cancelNewFile();
      return;
    }

    fileName.textContent = file.name;

    if (file.type.startsWith('image/')) {
      const reader = new FileReader();
      reader.onload = function (event) {
        previewImage.src = event.target.result;
        previewSection.style.display = 'block';
      };
      reader.readAsDataURL(file);
    } else {
      previewImage.src = '';
      previewSection.style.display = 'none';
    }
  });
});
