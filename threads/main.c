#include "main.h"

int main(int c, char *v[]) {
    parse_args(c, v);
    init_log();
    init_simulation();
    run_simulation();
    end_log();
    exit(0);
}

int parse_args(int c, char *v[]) {
    for (int i = 1; i < c; i += 2) {
        if (v[i][0] == '-') {
            switch (v[i][1]) {
                case ('s'):
                    s = atoi(v[i + 1]);
                    break;
                case ('p'):
                    p = atof(v[i + 1]);
                    break;
                case ('n'):
                    n = atof(v[i + 1]);
                    break;
                case ('e'):
                    seed_provided = true;
                    e = atoi(v[i + 1]);
                    break;
            }
        }
    }
    if (DEBUG_MODE) printf("Running simulation with parameters: s = %d, p = %f, n = %d, e = %d.\n", s, p, n, e);
}

void init_simulation(){
    if(seed_provided) srand(e);
    init_time_vars();
    init_plane_queues();
    init_atc();
}

void init_time_vars(){
    time_init = get_time();
    time_final = time_init + s;
}

void init_atc() {
    pthread_t atc_thread;
    atc_thread_args *args = malloc(sizeof(atc_thread_args));
    args->get_time = &get_time;
    args->time_final = time_final;
    args->landing_queue_empty = &landing_queue_empty;
    args->departing_queue_empty = &departing_queue_empty;
    args->emergency_plane_exists = &emergency_plane_exists;
    args->landing_queue_size = &landing_queue_size;
    args->departing_queue_size = &departing_queue_size;
    args->mut = &mut;
    args->cond_landing = &cond_landing;
    args->cond_departing = &cond_departing;
    pthread_create(&atc_thread, NULL, &atc_func, args);
}

void run_simulation(){
    // Insert initial planes.
    insert_plane(LANDING, get_sim_time, &mut, &cond_landing);
    insert_plane(DEPARTING, get_sim_time, &mut, &cond_departing);

    // Spawn planes according to PDF instructions.
    int emergency_counter = 0;
    while (get_time() < time_final) {
        float r = (float) rand() / (float) RAND_MAX;
        if (DEBUG_MODE) printf("[T=%d] r = %f\n", get_sim_time(), r);
        print_queues(n);
        if (r <= p)
            insert_plane(LANDING, &get_sim_time, &mut, &cond_landing);
        if (r <= 1 - p)
            insert_plane(DEPARTING, &get_sim_time, &mut, &cond_departing);
        emergency_counter++;
        if ((emergency_counter %= 40 * t) == 0)
            insert_plane(EMERGENCY, &get_sim_time, &mut, &cond_landing);
        pthread_sleep(t);
    }
}

int get_time() {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return tv.tv_sec;
}

int get_sim_time() {
    return get_time() - time_init;
}

void log_write(char *str) {
    fprintf(log_ptr, "%s", str);
}

void init_log() {
    log_ptr = fopen(LOG_FILENAME, "w");
    if (log_ptr == NULL) exit(1);
    init_plane_logger(&log_write, DEBUG_MODE);
    char *log_str = malloc(800);
    if(seed_provided)
        sprintf(log_str, "Plane data for simulation with parameters: [s = %d] [p = %f] [n = %d] [e = %d]:\n", s, p, n, e);
    else
        sprintf(log_str, "Plane data for simulation with parameters: [s = %d] [p = %f] [n = %d] [e = ?]:\n", s, p, n);
    log_write(log_str);
    log_write("=====================================================================================\n");
    log_write("ID\t\tStatus\t\tRequest Time\t\tRunway Time\t\tTurnaround Time\n");
}

void end_log(){
    struct plane_node *node;
    FOREACH_LANDING(node) log_plane_info(&node->plane.info);
    FOREACH_DEPARTING(node) log_plane_info(&node->plane.info);
    if (emergency_plane_exists()) log_plane_info(&emergency.info);
    log_write("=====================================================================================\n");
    fclose(log_ptr);
}

void print_queues(int n) {
    if (n != -1) {
        if (get_sim_time() >= n) {
            printf("[T=%d] ", get_sim_time());
            print_landing_queue();
            printf("[T=%d] ", get_sim_time());
            print_departing_queue();
        }
    }
}