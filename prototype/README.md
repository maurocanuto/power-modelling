# Power Modeller

This tool contains a power model generation for energy assessment of heterogeneous workloads that allows to estimate and predict power consumption of a server.
Different types of model have been implemented and can be used in order to develop a generic power model:

- Resources model: following this approach, the power consumption is computed as the sum of the power consumed by the main subsystems of a host:
	Ptot = Pcpu + Pmemory + Pdisk+ Pnetwork

- Global model: This approach does not consider each resource (CPU, memory, disk and network) separately but the relationship between two or more explanatory variables using all the data acquired during the micro-benchmarks execution as a training set.

- Combined model: The composed model is made up of 2 steps:
		1.	Generate a model for each subsystem
		2.	Combined the generated models using the same or a different algorithm

-----------------------------
========= Configuration ==========
-----------------------------

Configuration files, if not present, will be automatically created the first time the tool is executed. 
If the environment variable PMG_HOME is not set, the default directory /opt/PowerModelGenerator will be used.

-----------------------------
configuration.properties: 
-----------------------------
	csv-delimiter=,
	independent=powerWatts

	# If new-model is set to true, a new model is computed. Set the path of the model otherwise.
	new-model=true

	# If true data will be scaled and standardized
	scale-data=false

	# If new-model is set to false, set the path of the model to be used
	model-path=/temp-1428915018899/serializedGlobalModel.model
	# Model types available: resources, global, combined
	model-type=global

	# If resources model is used, up to what level do you want to build the model 
	# Possible options: cpu, cpu+mem, cpu+mem+disk, cpu+mem+disk+net
	# Note that the resulting model will be validated with the dataset specified in the 	#"validation"
	# property of datasets.properties.

	model-resources-level=cpu+mem+disk

	###############Model Options  ###############

	# Classifiers available: linearregression, reptree, multilayerperceptron, bagging
	classifier=multilayerperceptron

	# If combined model-type is chosen, set the algorithm to be used in the 2 step.
	#Classifiers available: linearregression, reptree, multilayerperceptron, bagging
	step2-classifier=linearregression

	# If preprocess-dataset is true, set the filter-type to be used
	preprocess-dataset=false
	#### Filter types available: movingaverage,removeidle ####
	filter-type=movingaverage

	##############Filter Options ################
	# if removeidle is chosen, set the idle power for training and validation set
	power-idle-training=75.0
	power-idle-validation=75.0
	# if movingaverage is chosen, set the window size
	moving-average-window=5
	datasets.properties: 

-----------------------------
datasets.properties
-----------------------------

Set the files path to be used in order to build the model and to validate it.
If model-type has been configured as global the “trainingGlobal” variable must be set.
Otherwise set the following variables: “trainingCPU”, “trainingMemory”, “trainingDisk”, “trainingNetwork” with the corresponding datasets to be used.

“validation” contains the path of the dataset to be validated.

Variables syntax: ${var1}, ${var2}

NOTE: All the datasets must contain the same variables.

-----------------------------
model-*.properties
-----------------------------

model-cpu.properties, model-memory.properties, model-disk.properties, model-network.properties

contain the variables to be used in the model building.

model-global.properties will be used if model-type has been configured as global.

All the variables must be contained in the csv. 

Syntax:
3 assignments can be done:
	metrics, var.NAME, newmetric.NAME

- metrics=METRIC1, METRIC2,....
METRIC1, METRIC2... must correspond to variables present in the csv headers. These variables will be used in the model generation.

-var.NAME=METRIC1+METRIC2...
This syntax can be used to build new metrics that can be used in the “newmetric.NAME” assignement. Metrics names existing in the csv must be included within squared brackets: E.g.  {cpu_user}.
These variables will NOT be used in the model generation unless used in the “newmetric” assignation. 

-newmetric.NAME={METRIC1}^2+{METRIC2}
It is possible to create new combinations of metric that will be used in the model generation.
Metrics names existing in the csv must be included within squared brackets: E.g. {cpu_user}.
Exponentiation, Squared roots and Logarithmic funcions can be used: E.g. log({cpu_user})

-----------------------------
Do not modify *.r files. They are used for filtering and error calculation.

-----------------------------
========= Execution ==========
-----------------------------

Requirements: Maven, Java 7
In the “prototype” directory run 'mvn package'.
Once successfully compiled and configured (see section above), run the tool:

java -cp bsc-powermodeller-1.0-SNAPSHOT.jar es.bsc.autonomic.powermodeller.Main 

Output:
After details about the model generated, several information about errors and correlation will be printed:

Correlation
R^2 - coefficient of determination R^2
MAE - Mean Absolute Percentage mmetric forecasting metric
RMSE - root mean squared error
RAE - relative absolute error
MAPE - Mean Absolute Percentage mmetric forecasting metric 

Serialized model and estimation file paths will be printed. The latter one is a csv containing two columns: “Pactual” (measured values), Ppredicted (preditcted valued).
A graph showing actual vs predicted power is also generated.
