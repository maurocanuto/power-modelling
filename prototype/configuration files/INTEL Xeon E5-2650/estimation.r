#!/usr/bin/env Rscript
args <- commandArgs(trailingOnly = TRUE)

library(rminer)
results <- read.csv(file=args[1],head=TRUE,sep=",")
x=results$Pactual
y=results$Ppredicted

cat("Correlation =", mmetric(y,x,"COR"), "\n")
cat("R^2 =", mmetric(y,x,"R2"), "\n")
cat("MAE =", mmetric(y,x,"MAE"), "\n")
cat("RMSE =", mmetric(y,x,"RMSE"), "\n")
cat("RAE =", mmetric(y,x,"RAE"), "\n")
cat("MAPE =",mmetric(y,x,"MAPE"), "\n")
