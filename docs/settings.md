# Settings

![Settings](/assets/settings.png)

Note: Self-explanatory settings are omitted.

## Request Editor
These options affect how the request editor displays requests and how it behaves when acting on sessions.
- Hide headers in the request editor:
    - Show all headers - Normal.
    - Hide common headers (default) - Hides very common headers, leaving only potentially interesting ones visible.
    - Show only changed headers - This will hide all headers unless they are being changed by a selected session.

- In the Switcher menu, show only sessions matching:
    - The request's subdomain - A request with host `foo.acme.com` will only list sessions that have their host set exactly to `foo.acme.com`
    - The request's top domain (default) - A request with host `foo.acme.com` will also list sessions with host `bar.acme.com`, `acme.com`, etc.
    - Show all Sessions - No filtering is performed, all stored sessions are listed in every request.

- When **updating cookies** from a request: - what to do when the "Update" button is pressed in the editor, regarding which cookies to copy.
    - Mirror request (default) - copies all cookies from the request, and removes any cookies already in the session.
    - Add all - copies all cookies from the request; existing cookies in the session are **kept**.
    - Update existing - only updates the value of cookies that are already in the session.
    - Do nothing - does not copy any cookie.
    
- When **updating headers** from a request: - what to do when the "Update" button is pressed in the editor, regarding which headers to copy.
    - Mirror request - copies all (uncommon) headers from the request, and removes any header already in the session.
    - Add all - copies all (uncommon) headers from the request; existing headers in the session are **kept**.
    - Update existing (default) - only updates the value of headers that are already in the session.
    - Do nothing - does not copy any header.

- When **injecting cookies** in a request: - what to do when a session is selected from the switcher, regarding which cookies to inject in the request.
    - Mirror session (default) - all cookies from the session are set in the request; existing cookies in the request are **removed**.
    - Add all - all cookies from the session are set in the request; existing cookies in the request are **kept**.
    - Update existing - only sets the value of cookies that are already in the request.
    - Do nothing - does not set any cookie.

- When **injecting headers** in a request: this option is not present; an `Add all` approach is always used.

## Auto Updater
- Stop after the first matching rule: For every request received by the proxy, the update rules are checked in order.
    - If this option **is set** (default), the extension will stop after the first matching rule, meaning a request can only ever trigger one rule.
    - If this option **is not set**, the extension will always try all the rules in sequence, regardless of whether one already matched, meaning a request can match (and trigger) multiple rules.

## Project data
This section allows you to delete the data stored in the project file, but does not remove the data from memory. Useful if the stored project file becomes corrupted or inconsistent.