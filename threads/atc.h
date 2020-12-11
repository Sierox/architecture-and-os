#include <stdbool.h>
#include <stdio.h>
#include <time.h>
#include <pthread.h>

/** Constant for determining what difference between number of planes in landing and departing queues cause overwhelming. */
#define OVERWHELMING 5

/** Argument structure for the ATC thread. Contains essential variables and function pointers for the ATC to work. */
typedef struct{
    int (*get_time)();
    int time_final;
    bool (*landing_queue_empty)();
    bool (*departing_queue_empty)();
    bool (*emergency_plane_exists)();
    int (*landing_queue_size)();
    int (*departing_queue_size)();
    pthread_mutex_t *mut;
    pthread_cond_t *cond_landing;
    pthread_cond_t *cond_departing;
} atc_thread_args;

/** Main function for the ATC thread. */
void* atc_func(void* args);
