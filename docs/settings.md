# Settings

![Settings](/assets/settings.png)

The settings window allows you set some settings to customize the behavior of the extension a bit. You can either set it for a single project or "globally" (i.e. persisting through different projects) depending on the tab you choose.

Note: Self-explanatory settings are omitted.

## Request Editor
These options affect how the request editor displays requests and how it behaves when acting on Sessions.
- **Hide headers in the request editor**
    - **Show all headers**: Normal.
    - **Hide common headers** (default): hides very common headers, leaving only potentially interesting ones visible.
    - **Show only changed headers**: this will hide all headers unless they are being changed by a selected Session.

- **In the Switcher menu, show only Sessions matching**
    - **The request's subdomain**: a request with host `foo.acme.com` will only list Sessions that have their host set exactly to `foo.acme.com`
    - **The request's top domain** (default): a request with host `foo.acme.com` will also list Sessions with host `bar.acme.com`, `acme.com`, etc.
    - **Show all Sessions**: no filtering is performed, all stored Sessions are listed in every request.

- **When updating cookies from a request**: what to do when the "Update from Request" button is pressed in the editor, regarding which cookies to copy.
    - **Mirror request** (default): cookies in the Session will reflect exactly cookies in the source request.
    - **Add all**: copies all cookies from the request; extra cookies already in the Session are **kept**.
    - **Update existing**: only updates the value of cookies that are already in the Session.
    - **Do nothing**: does not copy any cookie.
    
- **When updating headers from a request**: - what to do when the "Update from Request" button is pressed in the editor, regarding which headers to copy.
    - **Mirror request**: headers in the Session will reflect exactly headers in the source request (common headers excluded).
    - **Add all**: copies all (uncommon) headers from the request; extra headers already in the Session are **kept**.
    - **Update existing** (default): only updates the value of headers that are already in the Session.
    - **Do nothing**: does not copy any header.

- **When injecting cookies in a request**: - what to do when a Session is selected from the switcher, regarding which cookies to inject in the request.
    - **Mirror Session** (default): all cookies from the Session are set in the request; existing cookies in the request are **removed**.
    - **Add all**: all cookies from the Session are set in the request; existing cookies in the request are **kept**.
    - **Update existing**: only sets the value of cookies that are already in the request.
    - **Do nothing**: does not set any cookie.

- When **injecting headers** in a request: this option is not present; an `Add all` approach is always used.

## Auto Update Rules
- Stop after the first matching rule: For every request received by the proxy, the update rules are checked in order.
    - If this option **is set** (default), the extension will stop after the first matching rule, meaning a request can only ever trigger one rule.
    - If this option **is not set**, the extension will always try all the rules in sequence, regardless of whether one already matched, meaning a request can match (and trigger) multiple rules.

## Project data
- **Export to JSON**: export all the extension data in the current project to a JSON file. This includes Sessions (including cookies, headers, etc), Update Rules, and Settings.
- **Import from JSON**: import a previously exported JSON file with the project data. When possible, the extension will try to "merge" the data from the JSON export to the existing data.
- **Delete All Sessions**/**Delete All Update Rules**: self-explanatory.
- **Delete Everything**: delete all extension data from the project file; useful in case of issues/bugs.