Root cause

A Kubernetes cluster migration for this tenant was performed on Saturday, February 8th, 2025, at 9:00 PM PT. During the migration, non-standard steps were taken to accelerate tenant activation on the target cluster.

The PCU on the source cluster was deleted.

The tenant was scaled to 9 dedicated replicas.

min_num_rack_nodes_for_loading_data was set to ensure faster activation on the target cluster.Once migration was completed:

The PCU was recreated with min=1, max=1, and reserved=1.

New writers were successfully added.

However, min_num_rack_nodes_for_loading_data was not reset to 0, which hindered data reloads across new writers, as the actual number of writer replicas was lower than the configured min_num_rack_nodes_for_loading_data value.This missing step ultimately caused the incident.

Whys

Why was min_num_rack_nodes_for_loading_data set? To accelerate tenant activation on the target Kubernetes cluster by increasing replicas to 9.

Why was this additional step needed? The tenant contained 95 GB of metadata (no SAI), which was estimated to take around 30 minutes for the full outage phase—too long for the customer.

Why wasn’t min_num_rack_nodes_for_loading_data reset after migration? Post-migration, the PE team identified that the target cluster was not on the vSearch HF12 version, while the source cluster was. This version mismatch diverted focus toward checking potential compatibility issues, and the reset was overlooked, preventing data reload and leading to the incident.

Why did min_nodes=9 cause the issue? After the tenant was moved back to the PCU (configured with min=1, max=1, and reserved=1), the issue was triggered. The actual number of writer replicas was less than the configured min_num_rack_nodes_for_loading_data=9. When the number of active nodes falls below this value, the full SSTable reload does not occur. In this case, min_num_rack_nodes_for_loading_data was set to 9, but there was only 1 writer per rack. As a result, full data reload didn’t occur and writers continued serving reads with inconsistent data.

Why were no alerts triggered? Alerts weren’t triggered since there were no detectable anomalies—such as request failures, elevated latencies, or other metrics—that would have surfaced this issue.   