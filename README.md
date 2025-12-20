<div align="right">
 
![GitHub Action Badge](https://img.shields.io/github/actions/workflow/status/KyleGortych-SNHU/cs-305-software-security-prj-2/test.yml?label=main)

</div>

# Project 2

## Summery
This project shows the use of a self signed certificate for generating a
checksum for an uploaded file.

### Example 
<div align="center">
  <img src="https://raw.githubusercontent.com/KyleGortych-SNHU/cs-305-software-security-prj-2/refs/heads/main/screenshot1.jpg" width="100%" alt="img">
</div>

### OWASP Dependecy Check Results
<div align="center">
  <img src="https://raw.githubusercontent.com/KyleGortych-SNHU/cs-305-software-security-prj-2/refs/heads/main/screenshot2.jpg" width="100%" alt="img">
</div>

## Project Relfection
Briefly summarize your client, Artemis Financial, and its software
requirements. Who was the client? What issue did the company want you to
address?
    Artemis Financial is a company that deals with . Spceificly the client blank wanted to have file verification for .

What did you do well when you found your client’s software security
vulnerabilities? Why is it important to code securely? What value does software
security add to a company’s overall well-being?
    The main issue with the clients software was the deprecated springboot dependecy version. The reason for using the latest possible version is . It is important to ensure the code is secure as Artemis Finacial deals with IP and personal finacial data for its users. User trust is espesialy important in ensuring .

Which part of the vulnerability assessment was challenging or helpful to you?
    The vulnerability assesment was easy to resolve do to how the main dependecy springboot is tied to the majority of the transitive dependecies.

How did you increase layers of security? In the future, what would you use to
assess vulnerabilities and decide which mitigation techniques to use?
    One of the main layers of security that was added is using resultContainer.textContent in the javascript to prevent possible XSS and . Also the use of inputStream.read(buffer) in the backend prevents files uploaded from executing as they are read as binary data. To prevent denial of service I limited the size of the files that can be uploaded.  The use of codeql is a powerfull tool as it views the data flow rather than just signature checking .

How did you make certain the code and software application were functional and
secure? After refactoring the code, how did you check to see whether you
introduced new vulnerabilities?
    I made a unit test to check the functionality and security of the backend. The unitest checks spceificly for .

What resources, tools, or coding practices did you use that might be helpful in
future assignments or tasks?
    common practices that were used to ensure future maintence and .

Employers sometimes ask for examples of work that you have successfully
completed to show your skills, knowledge, and experience. What might you show
future employers from this assignment?
    I would show future employers of the use of the tools and tests created to
    ensure the functionality and safty of the . Part of the CI pipeline is the
    use of git actions to run the test file to show current status of specific
    branches for the repo. 

