Resolution summary

Set min_num_rack_nodes_for_loading_data to 0.

Rolled out writer nodes.

Enabled the preload cache.

Manually triggered cache preload rack by rack.

Verification

On the Writer dashboard, the PE team monitored:

Remote file cache disk usage

Relative file cache size percentage

Both metrics confirmed that data had returned to pre-migration levels.

Partition keys provided by the customer were verified on writers using the nodetool API.

After all data preloading was completed, the customer confirmed successful recovery.