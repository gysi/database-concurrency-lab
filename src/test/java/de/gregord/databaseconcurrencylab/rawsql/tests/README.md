Test Results:

| Tests                                  | ReadUncommitedTests    | ReadCommitedTests      | RepeatableReadTests     | Serializable             |
|----------------------------------------|------------------------|------------------------|-------------------------|--------------------------|
| Dirty reads                            | ❌ Mysql<br/>✅ Postgres | ✅ Mysql<br/>✅ Postgres | ✅ Mysql<br/>✅ Postgres  | ✅ Mysql<br/>✅ Postgres   |
| Repeatable reads                       | ❌ Mysql<br/>❌ Postgres | ❌ Mysql<br/>❌ Postgres | ✅ Mysql<br/>✅ Postgres  | ✅ Mysql<br/>✅ Postgres   |
| Phantom reads (single insert)          | ❌ Mysql<br/>❌ Postgres | ❌ Mysql<br/>❌ Postgres | ✅ Mysql<br/>✅ Postgres  | ✅ Mysql<br/>✅ Postgres   |
| Phantom reads (concurrent inserts)     | ❌ Mysql<br/>❌ Postgres | ❌ Mysql<br/>❌ Postgres | ✅ Mysql<br/>✅ Postgres  | ✅ Mysql<br/>✅ Postgres   |
| Phantom reads (without initial select) | ❌ Mysql<br/>❌ Postgres | ❌ Mysql<br/>❌ Postgres | ✅ Mysql<br/>✅ Postgres  | ❌ Mysql<br/>✅ Postgres   |
| Lost updates (single step)             | ✅ Mysql<br/>✅ Postgres | ✅ Mysql<br/>✅ Postgres | ✅ Mysql<br/>⚠️ Postgres | ✅ Mysql<br/>⚠️ Postgres  |
| Lost updates (with initial select)     | ✅ Mysql<br/>✅ Postgres | ✅ Mysql<br/>✅ Postgres | ✅ Mysql<br/>⚠️ Postgres | ⚠️ Mysql<br/>⚠️ Postgres |
| Lost updates (in two steps)            | ❌ Mysql<br/>❌ Postgres | ❌ Mysql<br/>❌ Postgres | ❌ Mysql<br/>⚠️ Postgres | ⚠️ Mysql<br/>⚠️ Postgres |

✅ = passed, ❌ = failed Assertion, ⚠️ = failed with Exception
