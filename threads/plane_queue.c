#include "plane_queue.h"

void init_plane_queues(){
    // Initialize queues.
    TAILQ_INIT(&landing_head);
    TAILQ_INIT(&departing_head);
    // "emergency" will be used as all the emergency planes. NONEXISTENT status means there are no emergency planes
    // at the moment. EMERGENCY status means there is one. Since there can be no more than one emergency plane at once
    // due to the slow spawn rate and their priority, there is no need for a queue.
    emergency.info.status = NONEXISTENT;
}

void insert_plane(char status, int (*get_sim_time)(), pthread_mutex_t* mut, pthread_cond_t* cond) {
    // Creation of the emergency planes.
    if(status == EMERGENCY){
        emergency.info.status = status;
        emergency.info.runway_time = -1;
        plane_thread_args* args = malloc(sizeof(plane_thread_args));
        args->info = &emergency.info;
        args->mut = mut;
        args->cond = cond;
        args->get_sim_time = get_sim_time;
        args->next_plane = &get_emergency_plane;
        args->action_func = &emergency_land_plane;
        args->emergency_plane_exists = &emergency_plane_exists;
        // Starting thread.
        pthread_create(&emergency.plane_thread, NULL, landing_func, args);
        return;
    }

    // Creation of normal landing/departing planes.
    struct plane_node *node = malloc(sizeof(struct plane_node));
    node->plane.info.status = status;
    node->plane.info.runway_time = -1;
    plane_thread_args* args = malloc(sizeof(plane_thread_args));
    args->info = &node->plane.info;
    args->mut = mut;
    args->cond = cond;
    args->get_sim_time = get_sim_time;
    args->emergency_plane_exists = &emergency_plane_exists;
    switch(status){
        // Specific to landing planes.
        case LANDING:
            args->next_plane = &get_landing_plane;
            args->action_func = &land_plane;
            // Starting thread.
            pthread_create(&node->plane.plane_thread, NULL, landing_func, args);
            // Insert to landing queue.
            TAILQ_INSERT_TAIL(&landing_head, node, next);
            break;
        // Specific to departing planes.
        case DEPARTING:
            args->next_plane = &get_departing_plane;
            args->action_func = &depart_plane;
            // Starting thread.
            pthread_create(&node->plane.plane_thread, NULL, departing_func, args);
            // Insert to departing queue.
            TAILQ_INSERT_TAIL(&departing_head, node, next);
            break;
    }
}

plane get_landing_plane() {
    return TAILQ_FIRST(&landing_head)->plane;
}

plane get_departing_plane() {
    return TAILQ_FIRST(&departing_head)->plane;
}

plane get_emergency_plane() {
    return emergency;
}

bool landing_queue_empty() {
    return TAILQ_EMPTY(&landing_head);
}

bool departing_queue_empty() {
    return TAILQ_EMPTY(&departing_head);
}

bool emergency_plane_exists(){
    return (emergency.info.status == EMERGENCY);
}

int landing_queue_size(){
    struct plane_node* p;
    int i = 0;
    TAILQ_FOREACH(p, &landing_head, next) i++;
    return i;
}

int departing_queue_size(){
    struct plane_node* p;
    int i = 0;
    TAILQ_FOREACH(p, &departing_head, next) i++;
    return i;
}

void land_plane() {
    TAILQ_REMOVE(&landing_head, landing_head.tqh_first, next);
}

void depart_plane() {
    TAILQ_REMOVE(&departing_head, departing_head.tqh_first, next);
}

void emergency_land_plane() {
    emergency.info.status = NONEXISTENT;
}

void print_landing_queue(){
    printf("Planes waiting/attempting to land: ");
    if(emergency_plane_exists())
        printf("%s ", plane_info_to_str(&emergency.info));
    struct plane_node* p;
    TAILQ_FOREACH(p, &landing_head, next)
        printf("%s ", plane_info_to_str(&p->plane.info));
    puts("");
}

void print_departing_queue(){
    printf("Planes waiting/attempting to depart: ");
    struct plane_node* p;
    TAILQ_FOREACH(p, &departing_head, next)
        printf("%s ", plane_info_to_str(&p->plane.info));
    puts("");
}