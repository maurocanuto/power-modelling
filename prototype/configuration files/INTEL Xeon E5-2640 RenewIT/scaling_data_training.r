#!/usr/bin/env Rscript

####################
#
# Parameters:
# 1 - File to scale
# 2 - Output file to store new dataset
# 3 - Independent variable
#
###################

args <- commandArgs(trailingOnly = TRUE)
training = read.csv(args[1], check.names=FALSE)

# Saving mu and sigma
get_scale <- function (x) {
  k = scale(x)
  return(attr(k,"scaled:scale"))
}
get_center <- function (x) {
  k = scale(x)
  return(attr(k,"scaled:center"))
}

scale_values <- data.frame(apply(training,2, get_scale))
center_values <- data.frame(apply(training,2, get_center))

training_scaled=scale(training)

#Shift to the right in order to avoid negative values
training_scaled <- training_scaled + 300

#Replacing original values for some columns
independent = args[5]
training_scaled[,independent] <- training[,independent]

if("timestamp" %in% colnames(training_scaled)){
  training_scaled[,"timestamp"] <- training$timestamp
}
if("Core_1CPU" %in% colnames(training_scaled)){
  training_scaled[,"Core_1CPU"] <- training$Core_1CPU
}
if("Core_2CPU" %in% colnames(training_scaled)){
  training_scaled[,"Core_2CPU"] <- training$Core_2CPU
}
if("numSockets" %in% colnames(training_scaled)){
  training_scaled[,"numSockets"] <- training$numSockets
}

# Remove NaN
training_scaled[is.na(training_scaled)] <- 0

training_scaled <- round(training_scaled,7)

write.csv(training_scaled, file = args[2],row.names=FALSE)
saveRDS(center_values, file = args[3])
saveRDS(scale_values, file = args[4])


