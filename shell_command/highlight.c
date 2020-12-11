#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include <ctype.h>

/**
 * Args:
 * {char word[], "yellow"/"green"/"pink/red/black/white/blue/purple"}* where each "word" is unique.
 *
 * Function:
 * Highlights each instance of each "word" with the corresponding "color" in the string taken from stdin.
 * If "word" A is a substring of "word" B, B will always be highlighted instead of the A inside B.
 * If there are colliding "word"s, the "word" which appears the earliest is highlighted.
 * The program is non-case-sensitive.
 * */

// Maximum bytes of text allowed to be read per line.
int MAX_BYTES_PER_LINE = 2048;

// Color codes.
char *DEFAULT = "\x1B[0m";
char *YELLOW = "\x1B[103m";
char *GREEN = "\x1B[42m";
char *PINK = "\x1B[105m";
char *RED = "\x1B[41m";
char *BLACK = "\x1B[40m";
char *WHITE = "\x1B[107m";
char *BLUE = "\x1B[44m";
char *PURPLE = "\x1B[45m";

// Returns the color code, given the color name.
char *get_color(char *name) {
    if (strcmp(name, "yellow") == 0)
        return YELLOW;
    else if (strcmp(name, "green") == 0)
        return GREEN;
    else if (strcmp(name, "pink") == 0)
        return PINK;
    else if (strcmp(name, "red") == 0)
        return RED;
    else if (strcmp(name, "black") == 0)
        return BLACK;
    else if (strcmp(name, "white") == 0)
        return WHITE;
    else if (strcmp(name, "blue") == 0)
        return BLUE;
    else if (strcmp(name, "purple") == 0)
        return PURPLE;
    else
        return DEFAULT;
}

void highlight_main(int argc, char *argv[]) {

    // If "word" A is a substring of "word" B, containers[i] = B and containees[i] = A.
    int *containers = malloc(sizeof(int) * argc / 2);
    int *containees = malloc(sizeof(int) * argc / 2);
    int contain_count = 0;

    // Initialization of containers and containees.
    for (int i = 0; i < argc; i += 2) {
        for (int j = 0; j < argc; j += 2) {
            if (i != j) {
                // If two "words" are the same, give an error and exit.
                if (strcmp(argv[i], argv[j]) == 0) {
                    fprintf(stderr, "Words with index %d and %d are the same!", i / 2, j / 2);
                    exit(1);
                }
                if (strstr(argv[i], argv[j]) != NULL) {
                    containers[contain_count] = i;
                    containees[contain_count++] = j;
                }
            }
        }
    }

    // Loop broken only if EOF is reached.
    while (true) {

        // Scan line.
        char str_in[MAX_BYTES_PER_LINE];
        fgets(str_in, MAX_BYTES_PER_LINE, stdin);
        strtok(str_in, "\n");

        // Check for EOF.


        if (strstr(str_in, "\t") != NULL) {
            printf("TAB\n");
        }


        if (feof(stdin))
            break;

        // Highlighting logic
        for (int s = 0; s < strlen(str_in); s++) {
            bool highlighted = false;
            for (int i = 0; i < argc; i += 2) {
                bool match = true;
                for (int w = 0; w < strlen(argv[i]); w++) {
                    if (tolower(str_in[s + w]) != argv[i][w]) {
                        match = false;
                    }
                }

                // Check for container match:
                // i.e. If a "word" which is a substring of another is found in the larger "word".
                if (match) {
                    bool container_match = false;
                    for (int c = 0; c < contain_count; c++) {
                        if (containees[c] == i) {
                            bool this_container_match = true;
                            for (int w = 0; w < strlen(argv[containers[c]]); w++) {
                                if (tolower(str_in[s + w]) != argv[containers[c]][w]) {
                                    this_container_match = false;
                                    break;
                                }
                            }
                            if (this_container_match) {
                                container_match = true;
                                break;
                            }
                        }
                    }
                    if (container_match)
                        continue;

                    // Success in identifying word to highlight! Printing with highlight:
                    printf("%s", get_color(argv[i + 1]));
                    for (int p = 0; p < strlen(argv[i]); p++) {
                        printf("%c", str_in[s + p]);
                    }
                    printf("%s", DEFAULT);
                    s += strlen(argv[i]) - 1;
                    highlighted = true;
                    break;
                }
            }

            // Print the non-highlighted letter, skip if successfully highlighted a word.
            if (!highlighted)
                printf("%c", str_in[s]);
        }
        printf("\n");
    }
    exit(0);
}