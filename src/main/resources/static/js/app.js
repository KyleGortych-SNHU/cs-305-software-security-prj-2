document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById('uploadForm');
    const fileInput = document.getElementById('file');
    const resultContainer = document.getElementById('result');

    // Form submission
    form.addEventListener('submit', function (event) {
        event.preventDefault(); // Prevent the default form submission

        const formData = new FormData();
        formData.append("file", fileInput.files[0]);

        // Loading message
        resultContainer.innerHTML = "Uploading...";

        // Send the file to the backend
        fetch('https://localhost:8443/hash-file', {
            method: 'POST',
            body: formData
        })
        .then(response => response.text())
        .then(data => {
            // Display the checksum result from the server
            resultContainer.innerHTML = data;
        })
        .catch(error => {
            // To handle errors that occur during the fetch request
            console.error('Error:', error);
            resultContainer.innerHTML = "An error occurred!";
        });
    });
});
