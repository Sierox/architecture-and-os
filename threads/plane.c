#include "plane.h"

void init_plane_logger(void (*log_write)(char *), bool _DEBUG_MODE) {
    plane_log_write = log_write;
    DEBUG_MODE = _DEBUG_MODE;
}

int next_landing_id() {
    landing_id += 2;
    return landing_id;
}

int next_departing_id() {
    departing_id += 2;
    return departing_id;
}

void *landing_func(void *args_) {
    plane_thread_args *args = (plane_thread_args *) args_;

    // Generate ID and request time for plane.
    args->info->id = next_landing_id();
    args->info->request_time = args->get_sim_time();
    if (DEBUG_MODE)
        printf("[T=%d] %s asks for permission to land.\n", args->info->request_time, plane_info_to_str(args->info));

    pthread_mutex_lock(args->mut);

    // Wait until either this plane is first in the landing queue and there are no emergency planes, OR this is an emergency plane.
    do pthread_cond_wait(args->cond, args->mut);
    while (!(((args->next_plane().info.id == args->info->id) && (!args->emergency_plane_exists())) ||
             (args->info->status == EMERGENCY)));

    if (DEBUG_MODE) printf("[T=%d] %s is landing.\n", args->get_sim_time(), plane_info_to_str(args->info));
    // Wait for 2t seconds, simulating the landing.
    pthread_sleep(2 * t);
    // "Landing" plane.
    args->action_func();

    pthread_mutex_unlock(args->mut);

    // Generate runway time for plane.
    args->info->runway_time = args->get_sim_time();

    if (DEBUG_MODE) printf("[T=%d] %s landed.\n", args->get_sim_time(), plane_info_to_str(args->info));
    // Log plane info.
    log_plane_info(args->info);

    pthread_exit(0);
}

void *departing_func(void *args_) {
    // Very similar to landing_func() so I will not comment this.
    plane_thread_args *args = (plane_thread_args *) args_;

    args->info->id = next_departing_id();
    args->info->request_time = args->get_sim_time();
    if (DEBUG_MODE)
        printf("[T=%d] %s asks for permission to depart.\n", args->info->request_time, plane_info_to_str(args->info));

    pthread_mutex_lock(args->mut);

    do pthread_cond_wait(args->cond, args->mut);
    while (args->next_plane().info.id != args->info->id);

    if (DEBUG_MODE) printf("[T=%d] %s is departing.\n", args->get_sim_time(), plane_info_to_str(args->info));
    pthread_sleep(2 * t);
    args->action_func();

    pthread_mutex_unlock(args->mut);

    args->info->runway_time = args->get_sim_time();

    if (DEBUG_MODE) printf("[T=%d] %s departed.\n", args->get_sim_time(), plane_info_to_str(args->info));
    log_plane_info(args->info);

    pthread_exit(0);
}

char *plane_info_to_str(plane_info *p) {
    char *str = malloc(0);
    sprintf(str, "<%d,%c>", p->id, p->status);
    return str;
}

void log_plane_info(plane_info *p) {
    char *str = malloc(800);
    char status;
    if (p->status == NONEXISTENT) status = EMERGENCY;
    else status = p->status;
    if (p->runway_time == -1)
        sprintf(str, "%d\t\t%c\t\t\t%d\t\t\t\t\tN/A\t\t\t\tN/A\n", p->id, status, p->request_time);
    else
        sprintf(str, "%d\t\t%c\t\t\t%d\t\t\t\t\t%d\t\t\t\t%d\n", p->id, status, p->request_time, p->runway_time,
                p->runway_time - p->request_time);
    plane_log_write(str);
}