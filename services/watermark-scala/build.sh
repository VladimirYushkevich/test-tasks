#!/bin/bash

sbt clean coverage test it:test coverageReport 'set test in assembly := {}' coverageOff assembly