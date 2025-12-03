Issue: 
Following a kube/cluster migration, some data was missing in Fiverr's customer-facing application due to an incorrect configuration on the new infrastructure.

Impact: Conversations between users on the Fiverr website were temporarily unavailable.

Root cause: This happened because the min_num_rack_nodes_for_loading_data setting was incorrectly left at 9 when the number of writer nodes was scaled down to 1, preventing writers from reloading sstables and resulting in unavailable data.

Steps to resolve: We reset min_num_rack_nodes_for_loading_data to 0, restarted the writer nodes, enabled cache preloading, and triggered manual preloading on each rack sequentially to hydrate the cache, restoring data access and confirming resolution with the customer.