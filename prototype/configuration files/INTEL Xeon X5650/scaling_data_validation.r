#!/usr/bin/env Rscript

####################
#
# Parameters:
# 1 - File to scale
# 2 - File containing scale values
# 3 - File containing center values
# 4 - Output file with scaling dataset
#
###################
args <- commandArgs(trailingOnly = TRUE)
validation = read.csv(args[1], check.names=FALSE)
center_values <- readRDS(paste(args[2],sep = ""))
scale_values <- readRDS(paste(args[3],sep = ""))

validation_scaled = mapply(function(x, y, z) scale((x), center=y, scale=z), (validation), (center_values[,1]), (scale_values[,1]))

#Shift to the right in order to avoid negative values
validation_scaled <- validation_scaled + 300

independent = args[5]
validation_scaled[,independent] <- validation[,independent]

if("timestamp" %in% colnames(validation_scaled)){
  validation_scaled[,"timestamp"] <- validation$timestamp
}
if("Core_1CPU" %in% colnames(validation_scaled)){
  validation_scaled[,"Core_1CPU"] <- validation$Core_1CPU
}
if("Core_2CPU" %in% colnames(validation_scaled)){
  validation_scaled[,"Core_2CPU"] <- validation$Core_2CPU
}
if("numSockets" %in% colnames(validation_scaled)){
  validation_scaled[,"numSockets"] <- validation$numSockets
}
validation_scaled[is.na(validation_scaled)] <- 0
validation_scaled[is.infinite(validation_scaled)] <- 0

validation_scaled <- round(validation_scaled,7)

write.csv(validation_scaled, file = args[4],row.names=FALSE)

