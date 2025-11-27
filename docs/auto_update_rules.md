# Auto Update Rules

Auto Update rules allow the user to automatically update a session based on Proxy requests that match specific conditions.

The goal is to keep a session always up to date during testing by tracking which requests passing through the Proxy are originating from a specific browser profile.

For example, if you use [PwnFox](https://addons.mozilla.org/it/firefox/addon/pwnfox/), you could create rules that watch for requests with the specified `X-PwnFox-Color` header and automatically update the corresponding session. Each Session will be kept in sync with the corresponding PwnFox profile. 

An easy alternative for Chrome-based browsers would be to use an extension that slightly changes the User-Agent; by using different Chrome profiles and adding a different text the User-Agent of each one, the extension can easily distinguish between them and associate each with a different session.

## Rules

![Rule Window](/assets/rule_window.png)

A rule is composed of the following parts:
- A set of **Conditions**
    - All the conditions that a request must match
- An associated **Session**
    - The session to update when all the conditions match
- Some configuration options
    - These behave the same as the ones in the Settings panel. Refer to [settings.md](/docs/settings.md) for more information.

## Conditions

The list of conditions in a rule is evaluated with a logical `AND`, meaning that all of them have to match for the update to occur.

If you need some conditions to match only in some situations (i.e., you need a logical `OR`), you can just duplicate the rule and set the alternative configuration there, since multiple rules can point to the same session.

![Condition Window](/assets/condition.png)

Each condition is made of:
- Type
    - What the conditions matches on
- Operation
    - How exactly the match is performed
- Pattern (Optional)
    - The string to match, only for types that need it
- A "negative match" option
    - When checked, it will invert the condition result (logical `NOT`)

### Available types
At the time of writing, the following condition types are available.

| Type            | Accepts Pattern | Matches Group   | Needs Response |
|-----------------|-----------------|----------------|----------------|
| Scope           | ❌               | ❌             | ❌              |
| Domain Name     | ✅               | ❌             | ❌              |
| URL             | ✅               | ❌             | ❌              |
| Protocol        | ❌               | ❌             | ❌              |
| Method          | ✅               | ❌             | ❌              |
| Request Header  | ✅               | ✅             | ❌              |
| Request Cookie  | ✅               | ✅             | ❌              |
| Request Body    | ✅               | ❌             | ❌              |
| Path            | ✅               | ❌             | ❌              |
| Query Parameter | ✅               | ✅             | ❌              |
| File Extension  | ✅               | ❌             | ❌              |
| Response Header | ✅               | ✅             | ✅              |
| Status Code     | ✅               | ❌             | ✅              |
| Response Body   | ✅               | ✅             | ✅              |

#### Pattern
Types that accept a pattern perform simple string matching operations on the value to match.

The following operations are available:
- Matches exactly
    - The source matches exactly the pattern (case-insensitive)
- Contains
    - The source contains the pattern (case-insensitive)
- Starts with
    - The source starts with the pattern (case-insensitive)
- Ends with
    - The source ends with the pattern (case-insensitive)
- Matches Regex
    - The source matches the pattern, which is interpreted as a regular expression (case-**sensitive**)

When **negative match** is set, the match result is simply inverted (logical `NOT`), except for group matches.

#### Group Matches

While most condition types (e.g. "URL") only match on a single source value, some conditions, such as "Request Header", need to match on multiple values, since, for instance, an HTTP request contains multiple headers.

For those types, the condition works as follows:
- If negative match **is not set**, the condition is true when **any value** in the group matches the pattern
    - e.g. the condition is true if at least one request header matches the pattern
- If negative match **is set**, the condition is true when **no value** in the group matches the pattern
    - e.g. the condition is true only if **no** request headers match the pattern

#### Matches on Response

Some of the available condition types (`Status Code`, `Response Header`, `Response Body`) match on a **response**, and not a request.

- When a Rule contains even one condition that matches on the HTTP response, the entire rule evaluation is postponed and triggered only when the corresponding response is received.
- Instead, rules that only contain conditions that match on the request will be evaluated as soon as possible, i.e. when the HTTP request is received by the proxy, regardless of the response.