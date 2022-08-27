# 🐲 Hydra

Combine Medusa with Hydra for modularity. 

[![Documentation Badge](https://img.shields.io/badge/Documentation-medusa--ui.gitbook.io%2Fdocs-informational)](https://medusa-ui.gitbook.io/docs/) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/bcc42d042ef5433aa6913ded9ba80da7)](https://www.codacy.com/gh/medusa-ui/hydra/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=medusa-ui/hydra&amp;utm_campaign=Badge_Grade)

The goal of Hydra is to be an entry point gateway proxy that allows you to deploy frontends modularly. It is a ready-for-use combination of a gateway with service discovery, load balancing, and JWT security. It uses proven frameworks like Spring Cloud Gateway and Service Discovery behind the scenes but streamlines their setup.

[![Hydra demo video](https://yt-embed.herokuapp.com/embed?v=RHFn0LW7bqw)](https://www.youtube.com/watch?v=RHFn0LW7bqw "Hydra demo")

---

Concept documentation: https://medusa-ui.gitbook.io/docs/hydra/concept

---
## Running the sample implementation locally
If you wish to use the Postgres sample implementation, set up a local postgres (https://www.postgresql.org/download/).

If neccesary, update values in the `application.yml`. 

```yaml
spring:
  r2dbc:
    url: "r2dbc:postgresql://localhost:5432/local_example"
    username: postgres
    password: pass123
```
We're using a reactive DB driver here, hence the r2dbc. Specific implementation is up to your discretion.

Then in the database, we need one table - to match with the user records:
```sql
CREATE TABLE public.hydra_user(
    id serial primary key,
    encoded_password text NOT NULL,
    username text NOT NULL,
    roles text NOT NULL DEFAULT ''::text,
    account_expired boolean NOT NULL DEFAULT false,
    account_locked boolean NOT NULL DEFAULT false,
    credentials_expired boolean NOT NULL DEFAULT false,
    enabled boolean NOT NULL DEFAULT true
);
```

No need to set up users yourself, the sample implementation adds a default user for you to use (and login form will be pre-filled with its credentials).