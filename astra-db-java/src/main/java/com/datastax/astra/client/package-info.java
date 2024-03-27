/**
 * Core Data API Client Classes. Each class represent a level to interact with the data API.
 *
 * <p><b>Usage Logic In a nutshell</b></p>
 * <div style="background: #f8f8f8; overflow:auto;width:auto;border:solid gray;border-width:.1em .1em .1em .2em;padding:.2em .6em;"><pre style="margin: 0; line-height: 125%"><span style="color: #408080; font-style: italic">// Initialize client</span>
 *DataAPIClient client <span style="color: #666666">=</span> <span style="color: #008000; font-weight: bold">new</span> DataAPIClient<span style="color: #666666">(</span><span style="color: #BA2121">&quot;token&quot;</span><span style="color: #666666">);;</span>
 *<br/><span style="color: #408080; font-style: italic">// Database (crud for collections) work with an assigned namespace</span>
 *Database db <span style="color: #666666">=</span> client<span style="color: #666666">.</span><span style="color: #7D9029">getDatabase</span><span style="color: #666666">(</span><span style="color: #BA2121">&quot;api_endpoint&quot;</span><span style="color: #666666">,</span> <span style="color: #BA2121">&quot;default_keyspace&quot;</span><span style="color: #666666">);</span>
 *db<span style="color: #666666">.</span><span style="color: #7D9029">createCollection</span><span style="color: #666666">(</span><span style="color: #BA2121">&quot;my_collection&quot;</span><span style="color: #666666">,</span> <span style="color: #666666">4,</span> SimilarityMetric<span style="color: #666666">.</span><span style="color: #7D9029">cosine</span><span style="color: #666666">);</span>
 *<br/><span style="color: #408080; font-style: italic">// Access to the data (crud for documents)</span>
 *Collection<span style="color: #666666">&lt;</span>Document<span style="color: #666666">&gt;</span> collection <span style="color: #666666">=</span> db<span style="color: #666666">.</span><span style="color: #7D9029">getCollection</span><span style="color: #666666">(</span><span style="color: #BA2121">&quot;my_collection&quot;</span><span style="color: #666666">);</span>
 *collection<span style="color: #666666">.</span><span style="color: #7D9029">insertOne</span><span style="color: #666666">(</span>Document<span style="color: #666666">.</span><span style="color: #7D9029">create</span><span style="color: #666666">(1).</span><span style="color: #7D9029">append</span><span style="color: #666666">(</span><span style="color: #BA2121">&quot;hello&quot;</span><span style="color: #666666">,</span> <span style="color: #BA2121">&quot;world&quot;</span><span style="color: #666666">));</span>
 *collection<span style="color: #666666">.</span><span style="color: #7D9029">insertOne</span><span style="color: #666666">(</span>Document<span style="color: #666666">.</span><span style="color: #7D9029">create</span><span style="color: #666666">(2).</span><span style="color: #7D9029">append</span><span style="color: #666666">(</span><span style="color: #BA2121">&quot;hello&quot;</span><span style="color: #666666">,</span> <span style="color: #BA2121">&quot;world&quot;</span><span style="color: #666666">),</span> <span style="color: #008000; font-weight: bold">new</span> <span style="color: #B00040">float</span><span style="color: #666666">[]</span> <span style="color: #666666">{.1</span>f<span style="color: #666666">,</span> <span style="color: #666666">.1</span>f<span style="color: #666666">,</span> <span style="color: #666666">.2</span>f<span style="color: #666666">,</span> <span style="color: #666666">.3</span>f<span style="color: #666666">});</span>
 *collection<span style="color: #666666">.</span><span style="color: #7D9029">findOne</span><span style="color: #666666">(</span>eq<span style="color: #666666">(1)).</span><span style="color: #7D9029">ifPresent</span><span style="color: #666666">(</span>System<span style="color: #666666">.</span><span style="color: #7D9029">out</span><span style="color: #666666">::</span>println<span style="color: #666666">);</span>
 *collection<span style="color: #666666">.</span><span style="color: #7D9029">deleteOne</span><span style="color: #666666">(</span>eq<span style="color: #666666">(1));</span>
 *<br/><span style="color: #408080; font-style: italic">// Access Database Administration (crud for namespaces)</span>
 *DatabaseAdmin dbAdmin <span style="color: #666666">=</span> db<span style="color: #666666">.</span><span style="color: #7D9029">getDatabaseAdmin</span><span style="color: #666666">(</span><span style="color: #BA2121">&quot;superUserToken&quot;</span><span style="color: #666666">);</span>
 *dbAdmin<span style="color: #666666">.</span><span style="color: #7D9029">createNamespace</span><span style="color: #666666">(</span><span style="color: #BA2121">&quot;namespace2&quot;</span><span style="color: #666666">);</span>
 *<br/><span style="color: #408080; font-style: italic">// Access the admin object (crud for databases)</span>
 *AstraDBAdmin astraDBAdmin  <span style="color: #666666">=</span> client<span style="color: #666666">.</span><span style="color: #7D9029">getAdmin</span><span style="color: #666666">(</span><span style="color: #BA2121">&quot;superAdminToken&quot;</span><span style="color: #666666">);</span>
 *</pre></div>
 */
package com.datastax.astra.client;
/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
