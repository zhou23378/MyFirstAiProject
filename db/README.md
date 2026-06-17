# Database Exports

This directory may contain local SQL exports used for migration or backup.

## Commit Policy

- `db/*.sql` is ignored by Git.
- Do not commit raw database exports.
- Raw exports may contain audit request parameters, login passwords, phone numbers, SMS verification messages, tokens, or other sensitive operational data.
- The canonical schema for development is `backend/src/main/resources/schema.sql`.
- If a shareable data fixture is needed, create a separate sanitized fixture with fake phones, fake names, no audit request body, no tokens, and no real login/password samples.

## Current Local Findings

Local files such as `salon_data_only.sql` include sensitive test data, especially `audit_log` request params with login payloads and member/notification phone numbers. Keep them local only.
