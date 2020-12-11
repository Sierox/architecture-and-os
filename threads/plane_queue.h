#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <sys/queue.h>
#include <stdbool.h>

#include "plane.h"

/** Structure for each node of the plane queues. Contains a plane and a pointer to the next plane. */
struct plane_node {
    plane plane;
    TAILQ_ENTRY(plane_node) next;
};

/** Heads of the landing and departing queues. */
TAILQ_HEAD(landing_head, plane_node) landing_head;
TAILQ_HEAD(departing_head, plane_node) departing_head;

/** Object to be used as the emergency plane. */
plane emergency;

/** Foreach definitions for landing and departing queues (will be used for logging planes in queues). */
#define	FOREACH_LANDING(plane_node)					\
	TAILQ_FOREACH(plane_node, &landing_head, next)
#define	FOREACH_DEPARTING(plane_node)					\
	TAILQ_FOREACH(plane_node, &departing_head, next)

/** Initializes queues and emergency plane object. */
void init_plane_queues();

/** Creates a plane_node object with status (char status) and inserts it to the appropriate queue. */
void insert_plane(char status, int (*get_sim_time)(), pthread_mutex_t* mut, pthread_cond_t* cond);

/** Returns the first plane in the landing queue. */
plane get_landing_plane();
/** Returns the first plane in the departing queue. */
plane get_departing_plane();
/** Returns the emergency plane object. */
plane get_emergency_plane();

/** Returns true if landing queue is empty, false otherwise. */
bool landing_queue_empty();
/** Returns true if departing queue is empty, false otherwise. */
bool departing_queue_empty();
/** Returns true if emergency plane exists, false otherwise. */
bool emergency_plane_exists();

/** Returns size of landing queue. */
int landing_queue_size();
/** Returns size of landing queue. */
int departing_queue_size();

/** Removes the first element in the landing queue, action_func for landing planes. */
void land_plane();
/** Removes the first element in the departing queue, action_func for departing planes. */
void depart_plane();
/** Sets emergency plane status to NONEXISTENT, action_func for emergency planes. */
void emergency_land_plane();

/** Prints planes in landing queue. */
void print_landing_queue();
/** Prints planes in departing queue. */
void print_departing_queue();

