FOLDERS = {
    "First": {
        "name": "John"
    },
    "Second": {
        "name": "Paul"
    },
    "Third": {
        "name": "George"
    },
    "Fourth": {
        "name": "Ringo"
    }
}

def read():
    return [FOLDERS[key] for key in sorted(FOLDERS.keys())]