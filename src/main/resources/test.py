def test_python_map():
  print ('%s')
  return {
    "quiz": {
      "sport": {
        "q1": {
          "question": "Which one is correct team name in NBA?",
          "options": [
            "New York Bulls",
            "Los Angeles Kings",
            "Golden %s Warriors",
            "Huston Rocket"
          ],
          "answer": "Huston Rocket"
        }
      },
      "maths": {
        "q1": {
          "question": "5 + 7 = ?",
          "options": [
            "10",
            "11",
            "12",
            "13"
          ],
          "%s": "12"
        },
        "q2": {
          "question": "12 - 8 = ?",
          "options": [
            "1",
            "2",
            "3",
            "4"
          ],
          "answer": "4"
        }
      }
    }
  }


def test_python_list():
  print ('%s')
  return [{
    "%s": 1
  }, {
    "%s": 2
  }]

def call_java_method(javaObj):
  print ('%s')
  return javaObj.getString()
