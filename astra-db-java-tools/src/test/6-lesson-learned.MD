What went well

The DB core team, support, and PE collaborated to identify the root cause and implement the resolution.

What didn't go so well

Escalations workflow didn't work ( Based on feedback  from DM)

The PE on-call engineer was new and unfamiliar with this type of issue, which contributed to a longer mitigation time.

Areas for improvement

Any additional settings added during migrations, need to be taken off after they finish.

Make sure to monitor RemoteFileCacheDiskUsage size after the migration till it reaches pre-migration level.

Issue triggered has been addressed in vSearch HF12. The fix ensures that, instead of returning no data, the system now returns an error.

Fix details https://github.com/riptano/cndb/pull/15705 and https://github.com/riptano/cndb/issues/15673

Key takeaways

Migration acceleration can introduce risks, and the runbook was updated to address these risks.

The issue was related to data inaccessibility, not actual data loss.