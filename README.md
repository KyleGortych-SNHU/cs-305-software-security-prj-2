<div align="right">
 
![GitHub Action Badge](https://img.shields.io/github/actions/workflow/status/KyleGortych-SNHU/cs-305-software-security-prj-2/test.yml?label=main)

</div>

# Project 2

## Summery
This project shows the use of a self signed certificate and implementation of generating a
checksum for an uploaded file. The certificate was made via Java's Keytool commands,

```bash
keytool -genkeypair -keyalg RSA -keysize 2048 -alias selfsigned -keystore keystore.jks -storepass SNHU12 -keypass SNHU12 -validity 360

keytool -export -alias selfsigned -storepass SNHU12 -file server.cer -keystore keystore.jks
```

### Successfully ran 

<details>
    <summary>Click to view</summary>

    <div align="center">
      <img src="https://raw.githubusercontent.com/KyleGortych-SNHU/cs-305-software-security-prj-2/refs/heads/main/screenshot1.jpg" width="100%" alt="img">
    </div>

</details>

### OWASP Dependecy Check Results

<details>
    <summary>Click to view</summary>

    <div align="center">
      <img src="https://raw.githubusercontent.com/KyleGortych-SNHU/cs-305-software-security-prj-2/refs/heads/main/screenshot2.jpg" width="100%" alt="img">
    </div>

</details>

### Example of SAST Usage

<details>
    <summary>Click to view</summary>

    <div align="center">
      <img src="https://raw.githubusercontent.com/KyleGortych-SNHU/cs-305-software-security-prj-2/refs/heads/main/screenshot3.jpg" width="100%" alt="img">
    </div>

</details>

## Project Relfection

### Client Overview
Briefly summarize your client, Artemis Financial, and its software
requirements. Who was the client? What issue did the company want you to
address?

Artemis Financial is a company that deals with financial services and sensitve client data. Spceificly the client wanted to add a file verification step to its web application to ensure secure communications. When the web application is used to transfer data, the company will need a data verification step in the form of a checksum.

To start I had to look at the current code base and assess what needed to be implemented and updated to current best practices.

### Resolving Vulnerabilities in Code Base
What did you do well when you found your client’s software security
vulnerabilities? Why is it important to code securely? What value does software
security add to a company’s overall well-being?

The main issue with the clients software was the deprecated springboot dependecy version. The reason for using the latest possible version is it can resolve the majority of transitive dependecies vulnerabilities. It is important to ensure the code is secure as Artemis Finacial deals with IP and personal finacial data for its users. User trust is espesialy important in ensuring client trust and expectations.

### Challenges in Assessment
Which part of the vulnerability assessment was challenging or helpful to you?

The vulnerability assesment was easy to resolve do to how the main dependecy springboot is tied to the majority of the transitive dependecies. The main challenge was the testing and resolving import & dependecy mismatch and deprecated methods. 

### The Improved Layers of Security
How did you increase layers of security? In the future, what would you use to
assess vulnerabilities and decide which mitigation techniques to use?

To increase the layers of security I implemented these measures:
- **Preventing XSS Cross site Scripting**: `resultContainer.textContent` was used in the JavaScript to sanitize user inputs and prevent script injection.
- **File Upload Security Checks**: I used `inputStream.read(buffer)` in the backend to read uploaded files a s binary data to prevent them from being executed.
- **Denial of Service Prevention**: Lastly the size of the files that can be uploaded has been limited to prevent overloading the system.

For future vulnerability scanning I would use tools that go more in-depth than SAST such as CodeQL and its integration in githubs CI pipeline git actions.

### Verification of Code Base Security
How did you make certain the code and software application were functional and
secure? After refactoring the code, how did you check to see whether you
introduced new vulnerabilities?

I made a unit test to check the functionality and security of the backend. 
The unitest checks spceificly for:
- **Secure HTTPS Connections**: Verified the use of proper TLS and SSL encryption.
- **File Upload Security**: Added test casses for 
    - Reject empty files via `uploadEmptyFile_isHandledSafely`.
    - Reject Oversized files via `oversizedFile_isRejected`.
    - Handling of files with malicious content such as filenames with script tags or special characters to prevent XSS attacks via `maliciousFilename_isSanitizedCorrectly` and `unicodeFilename_isSanitizedCorrectly`. 
- **Filename Sanitization**: Verified that file names containing spaces, special characters, or unicode characters are sanitized correctly to prevent directory traversal or other attacks via `filenameWithSpaces_isSanitizedCorrectly` and `filenameWithSpecialSymbols_isSanitizedCorrectly`.

- **Checksum Verification**: Ensured that files are properly hashed and checksums are accurately calculated via `checksumIsCorrectForKnownFile`.

I also used `TestRestTemplate` to simulate file uploads over HTTPS and verify the server's response to different types of input. In adding these test cases it gives a starting point in giving the application full coverage.  

### Tools and Coding Practices
What resources, tools, or coding practices did you use that might be helpful in
future assignments or tasks?

Common practices that were used to ensure future maintence and . For tools I used OWASP Dependency Check to ensure dependecies are up to date and don't have any vulnerabilities. For SAST I used opengrep to scan the code base. I also setup the git action to run the unit test I made and used maven as the build tool.

### Showcasing My Work
Employers sometimes ask for examples of work that you have successfully
completed to show your skills, knowledge, and experience. What might you show
future employers from this assignment?

I would show future employers of the use of the tools and tests created to ensure the functionality and safty of the application. Part of the CI pipeline is the use of git actions to run the test file to show current status of specific branches for the repo. Lastly I would show the process of using SAST tools during my workflow and change in design. 
