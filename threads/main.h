#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <sys/time.h>

#include "atc.h"
#include "plane_queue.h"
#include "pthread_sleep.h"

/** Constants */
#define LOG_FILENAME "planes.log"
#define DEBUG_MODE false

/** Argument variables. */
 /** s: simulation time */
int s = 60;
/** p: probability (concerning plane spawning) */
float p = 0.5f;
/** n: console log start step */
int n = -1;
/** e: rng seed */
int e;

bool seed_provided = false;

/** Pointer to the log file. */
FILE *log_ptr;

/** Time variables. */
int time_init;
int time_final;

/** Synchronization variables. */
pthread_mutex_t mut = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t cond_landing = PTHREAD_COND_INITIALIZER;
pthread_cond_t cond_departing = PTHREAD_COND_INITIALIZER;

/** Parses arguments and initializes argument variables. */
int parse_args(int c, char *v[]);

/** Initializes simulation. */
void init_simulation();

/** Initializes the time variables. */
void init_time_vars();

/** Initializes the ATC thread. */
void init_atc();

/** Runs simulation, i.e. spawn planes. */
void run_simulation();

/** Returns seconds since EPOCH as int. */
int get_time();
/** Returns seconds since the beginning of the simulation as int. */
int get_sim_time();

/** Initialize the log. */
void init_log();
/** Writes (char *str) to the log. */
void log_write(char *str);
/** Ends log. */
void end_log();

/** Prints landing/departure queues to the terminal. */
void print_queues(int n);