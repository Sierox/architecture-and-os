#include "atc.h"

void *atc_func(void *args_) {
    atc_thread_args *args = (atc_thread_args *) args_;
    while (args->get_time() < args->time_final) {
        pthread_mutex_lock(args->mut);
        // Starvation solution in the PDF (commented out since mutually exclusive with my solution):
        // bool departing_overwhelming = args->departing_queue_size() >= 5;

        // My starvation solution:
        // Departing planes are prioritized if number of landing planes exceed number of departing planes by OVERWHELMING or more.
        bool departing_overwhelming = args->departing_queue_size() - args->landing_queue_size() >= OVERWHELMING;

        // If there are planes in the landing queue and if the departing queue is not overwhelming OR a plane needs emergency landing, signal landing cond.
        // Else if there are planes in the departing queue, signal the departing cond.
        if ((!args->landing_queue_empty() && !departing_overwhelming) || args->emergency_plane_exists())
            pthread_cond_signal(args->cond_landing);
        else if (!args->departing_queue_empty())
            pthread_cond_signal(args->cond_departing);
        pthread_mutex_unlock(args->mut);
    }
    pthread_exit(0);
}