#!/bin/bash

kubectl create namespace local
kubectl apply -f deployment/service.yaml
kubectl apply -f deployment/deployment.yaml