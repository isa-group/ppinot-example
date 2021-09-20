# PPINOT Example

This is a project that illustrates how to use [PPINOT](https://github.com/isa-group/ppinot) to compute Process Performance Indicators using an event log. 

It includes just a class `App` that specifies the log and eight different PPIs with varying degrees of complexity. The log used to compute PPIs is in MXML format and was obtained from the results of a simulation using [BIMP](https://bimp.cs.ut.ee/).

The eight PPIs defined are:

- Response Time: The time between the start of the process and the activity `Plan FI` considering only working hours.

- Presence Time: The time between the instant where `Plan FI` finishes and the instant where `Go to venue finishes` considering only working hours.

- Resolution Time: The time between the instant where activity `Perform FI` starts and finishes considering only working hours.

- Documentation Time: The time between the instant where activity `Create and submit FI documentation` starts and finishes considering only working hours. It takes pairs of starts and ends and aggregates them.

- Plan vs Total: The percentage of response time with respect to the sum of response, presence and resolution time.

- Max corrections: The maximum number of corrections found in a process instance grouped by a period of time (monthly)

- Average duration: The average duration of each process instance considering only working hours and grouped by a period of time (monthly).

- Percentage accomplished: The percentage of process instances whose response time, presence time, resolution time and documentation time finished on time (less than 0.5, 4, 2, and 4 working hours, respectively).
