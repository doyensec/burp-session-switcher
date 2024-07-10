# Burp Session Switcher

[![Doyensec Research Island](https://img.shields.io/static/v1?logo=data:image/png;base64,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&link=https://doyensec.com/research.html&message=Research%20Island&&label=Doyensec&color=purple)](https://doyensec.com/research.html)


## :rocket: Introduction

Session Switcher is a Burp Suite extensions that allows to easily switch the session (Cookies, headers, etc) of a HTTP request on the fly, tailored for targeted manual authorization testing.

## :star2: Features

WIP

#### :mag_right:  How to use it

WIP


# :arrow_down: Installation
This extension is built on Montoya API v2023.12.1. It will not work on older versions of Burp Suite.

# :computer: Building the Session Switcher extension from git

1. Install Java 17+. For example, in Debian-based distros:

```bash
$ sudo apt install -y openjdk-17-jdk
$ java --version
openjdk 17.0.6 2023-01-17
```

2. Clone the repo:

```bash
$ git clone https://github.com/doyensec/burp-session-switcher
$ cd burp-session-switcher
```

3. Build the Session Switcher extension:

```bash
$ ./gradlew build
```

Load the file `SessionSwitcher.jar` into Burp as a Java extension.


# :handshake: Contributing

The Session Switcher Burp Extension thrives on community contributions. Whether you're a developer, researcher, designer, or bug hunter, your expertise is invaluable to us. We welcome bug reports, feedback, and pull requests. Your participation helps us continue to improve the extension, making it a stronger tool for the community.

Interactions are best carried out through the GitHub issue tracker, but you can also reach us on social media ([@Doyensec](https://twitter.com/Doyensec)). We look forward to hearing from you!

# :busts_in_silhouette: Contributors

A special thanks to our contributors. Your dedication and commitment have been instrumental in making this extension what it is today.

Current:
- **Maintainer:** Savio Sisco [@lokiuox (Github)](https://github.com/lokiuox)

This project was made with support of [Doyensec](https://doyensec.com/research.html).


![Doyensec Research](docs/doyensec_logo.svg)   


