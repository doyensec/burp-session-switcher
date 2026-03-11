# Auto Update Rules

Auto Update rules allow the user to set specific conditions to track Proxy requests and automatically update a session based on matching ones.

The goal is to keep a session always up to date during testing by automatically tracking which requests passing through the Proxy "belong" to which application user.

## Examples

Here are some examples on how to track requests and associate them to specific sessions.

### Track Header
Easiest situation: all app requests coming from user `alice` have an header such as `X-MyApp-User: 12345`. You can easily track them by adding a rule with the following condition:
- **Type:** Request Header
- **Operation**: Matches exactly
- **Pattern**: `X-MyApp-User: 12345`

### Track Cookie
Similar to the previous example, if all requests from `alice` have a cookie such as `current_user=alice`, you can use the following condition:
- **Type:** Request Cookie
- **Operation**: Matches exactly
- **Pattern**: `current_user=alice`

### JWT Claim
Let's say the app does not send any header/cookie that could easily identify who the session belongs to, but there's a JWT in a header or cookie. You can create a condition to match a specific **JWT Claim**, i.e. an attribute from the JWT payload part.

For example, a request with the following header:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwidXNlciI6ImFsaWNlIiwiYWRtaW4iOnRydWV9.fTaNzup5syoeGbjfKdF6ZsqD9Fkp2U4mJ6ySVTaelQw
```

where the JWT payload decodes to the following:
```json
{
  "sub": "1234567890",
  "user": "alice",
  "admin": true
}
```

will be matched by the following rule condition:

- **Type:** JWT Claim
- **Operation**: JWT in Header
- **Header/Cookie Name**: `Authorization` (just the header name, the `Bearer` part is skipped automatically)
- **Claim Name**: `user`
- **Match Type**: Matches exactly
- **Claim Value**: `alice`

### User-Agent
A method to track sessions without relying on specific request data is to use different browser profiles for different app users, and change the User-Agent of each profile to something unique using a browser extension.

For example, you could add `BurpSession/profile1` to the User-Agent of one browser profile, and `BurpSession/profile2` to another. You can then track the requests with a simple rule condition:

- **Type:** User Agent
- **Operation**: Contains
- **Pattern**: `BurpSession/profile1`

### PwnFox (or similar extensions)

On the same note as the previous example, if you use [PwnFox](https://addons.mozilla.org/it/firefox/addon/pwnfox/) or other similar extensions, you could create rules that watch for requests with the specified `X-PwnFox-Color` header and automatically update the corresponding session. This way you can associate PwnFox tabs to specific Sessions.

## Details

### Update Options
The options in the "Update Rule" window allow you to set some configuration that applies only to this specific rule:
- **Session to update**: pretty self-explanatory; in which Session to save the headers/cookies from a request that matches all the conditions.
- **Cookie update mode**: specify which cookies to save from the request (see below).
- **Headers update mode**: specify which headers to save from the request (see below).
- **Highlight color**: if a color is selected, matching requests are highlighted in the proxy with that color.

#### Update Modes
Here is what each header/cookie "update mode" does:
- **Mirror request**: the headers/cookies in the Session will be identical to the ones in the matched request. 
- **Add all**: all cookies from the request are saved in the Session. Any extra cookie already in the Session is kept.
- **Update existing**: only cookies that are already in the Session are updated from the matched request (if their value changed); extra cookies in the request are ignored.
- **Do nothing**: don't copy any cookie/header from the request.

Note that, of course, setting "Do nothing" as both cookies and headers update mode will make the Update Rule do effectively nothing (except maybe highlighting a matching request, if you set an highlight color).

### Conditions

The list of conditions in a rule is evaluated with a logical `AND`, meaning that all of them have to match for the update to occur. The conditions are evaluated in order, therefore it's better to put stricter conditions first for performance reasons.

If you need some conditions to match only in some situations (i.e., you need a logical `OR`), you can just duplicate the rule and set the alternative configuration there, since multiple rules can point to the same session.

![Condition Window](/assets/condition.png)

Each condition is made of:
- **Type**
    - What the conditions matches on
- **Operation**
    - How exactly the match is performed
- **Pattern (Optional)**
    - The string to match, only for types that need it
- A "**negative match**" option
    - When checked, it will invert the condition result (logical `NOT`)

### Available types
At the time of writing, the following condition types are available.

- Scope
- Domain Name
- URL
- Protocol
- Method
- Request Method
- Request Header
- Request Cookie
- Request Body
- User Agent
- Path
- Request Parameter
- File Extension
- JWT Claim
- Response Header
- Response Status Code
- Response Body

#### Pattern
Types that accept a pattern perform simple string matching operations on the value to match.

The following operations are available:
- **Matches exactly**
    - The source matches exactly the pattern (case-insensitive)
- **Contains**
    - The source contains the pattern (case-insensitive)
- **Starts with**
    - The source starts with the pattern (case-insensitive)
- **Ends with**
    - The source ends with the pattern (case-insensitive)
- **Matches Regex**
    - The source matches the pattern, which is interpreted as a regular expression (case-**sensitive**)

When **negative match** is set, the match result is simply inverted (logical `NOT`), except for group matches.

#### Matching Multiple Values

For condition types that match on multiple values, such as "Request Header" or "Request Cookie" (since an HTTP request contains multiple headers and cookies), the condition will be true if **any** of the value matches the condition.

On the other hand, if **negative match** is selected, the condition is true when **no value** in the group matches the pattern

#### Matches on Response

Some of the available condition types (`Status Code`, `Response Header`, `Response Body`) match on a **response**, and not a request.

When a Rule contains even one condition that matches on the HTTP response, the entire rule evaluation is postponed and triggered only when the corresponding response is received.

Normally, rules that only contain conditions matching on the **request** will be evaluated as soon as possible, i.e. when the HTTP request is received by the proxy, regardless of when the response is received.