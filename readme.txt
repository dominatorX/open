Many big raw files cannot be uploaded. After processing, the files for default parameters are uploaded.

-----------1. You need to download the NYC taxi data first:
wget -P ./NYC/ https://s3.amazonaws.com/nyc-tlc/trip+data/yellow_tripdata_2013-12.csv

-----------2. Then run maven command for request generation and initial LRU of normal graph 
Run:    mvn compile exec:java -Dexec.mainClass="get_request"

-----------3. Then setup LRU for HMPO graph
Run:    mvn compile exec:java -Dexec.mainClass="ShortestPathHSG"

-----------4. Finally we have 6 algorithms
-----for Algorithm SMDB, run:
Run:    mvn compile exec:java -Dexec.mainClass="SMDB" -Dexec.args="#_of_worker detour_factor capacity"
-----for Algorithm HSRP, run:
Run:    mvn compile exec:java -Dexec.mainClass="HSRP" -Dexec.args="#_of_worker detour_factor capacity"
-----for Algorithm BasicMP, run:
Run:    mvn compile exec:java -Dexec.mainClass="BasicMP" -Dexec.args="#_of_worker detour_factor capacity"
-----for Algorithm GreedyDP, run:
Run:    mvn compile exec:java -Dexec.mainClass="GreedyDP" -Dexec.args="#_of_worker detour_factor capacity"
-----for Algorithm FirstServe, run:
Run:    mvn compile exec:java -Dexec.mainClass="FirstServe" -Dexec.args="#_of_worker detour_factor capacity"
-----for Algorithm KineticTree, run:
Run:    mvn compile exec:java -Dexec.mainClass="KineticTree" -Dexec.args="#_of_worker detour_factor capacity"

-----------For example, 10000 worker with capacity 3, allowing detour 30%:
mvn compile exec:java -Dexec.mainClass="SMDB" -Dexec.args="10000 1.3 3"



Below are the steps to set up with new graphs. Remember to change the data name in each java file.
1.Change the path of pom.xml into your own desk's

2.Put the files for nodes and edges of map from OSM in the project file.

----------for basic graph generation-----------------------------
3.Run:       mvn compile exec:java -Dexec.mainClass="Setup"

----------for NYC request generation and LRU initial-------------
4.Run:    mvn compile exec:java -Dexec.mainClass="get_request.py"

----------for grid prune------------------------------------------
5.Run:       mvn compile exec:java -Dexec.mainClass="grid_dis"

----------operations for meeting point based solutions----------
6.Run:       mvn compile exec:java -Dexec.mainClass="ProcessWM"

----------generate synthetic data---------------------------------
7.Run:       mvn compile exec:java -Dexec.mainClass="get_syn"
