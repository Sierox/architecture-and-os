#include <stdio.h>
#include <pthread.h>
#include <sys/queue.h>
#include <stdbool.h>
#include "pthread_sleep.h"
#include <stdlib.h>

/** The t constant. Defined in plane since t is associated with the landing/departing time of planes. */
#define t 1

/** Plane state constants. */
#define LANDING 'L'
#define DEPARTING 'D'
#define EMERGENCY 'E'
#define NONEXISTENT 'N'

/** Function pointer to log_write function. */
static void (*plane_log_write)(char*);

/** DEBUG_MODE, taken from main. */
static bool DEBUG_MODE;

/** ID counter variables. */
static int landing_id = -2;
static int departing_id = -1;

/** Struct which holds all essential information about a plane. */
typedef struct{
    int id;
    char status;
    int request_time;
    int runway_time;
} plane_info;

/** The main plane struct containing a thread for communicating with the ATC, and a plane_info object. */
typedef struct{
    pthread_t plane_thread;
    plane_info info;
} plane;

/** Argument structure for the plane threads. Contains essential variables and function pointers for planes to work. */
typedef struct {
    plane_info* info;
    pthread_mutex_t* mut;
    pthread_cond_t* cond;
    plane (*next_plane)();
    // action_func is the function called after the plane lands/departs (to remove it from the queue).
    void (*action_func)();
    int (*get_sim_time)();
    bool (*emergency_plane_exists)();
} plane_thread_args;

/** Initializes log for planes. */
void init_plane_logger(void (*log_write)(char*), bool DEBUG_MODE);

/** ID assignment function for landing planes. */
int next_landing_id();

/** ID assignment function for departing planes. */
int next_departing_id();

/** The main function of landing (and emergency landing) planes. */
void* landing_func(void* args);

/** The main function of departing planes. */
void* departing_func(void* args);

/** Takes in a plane_info object and returns "<ID, STATUS>". */
char* plane_info_to_str(plane_info* p);

/** Logs a detailed description of the plane. */
void log_plane_info(plane_info* p);

