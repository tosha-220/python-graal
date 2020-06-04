function test_python_map() {
  console.log("%s");
  return {
    "quiz": {
      "sport": {
        "q1": {
          "question": "Which one is correct team name in NBA?",
          "%s": [
            "New York Bulls",
            "Los Angeles Kings",
            "Golden State Warriors",
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
          "answer": "%s"
        }
      }
    }
  }
}

function test_python_list() {
  console.log("%s");
  return [{
    "%s": 1
  }, {
    "field2": 2
  }]
}

function call_java_method(javaObj) {
  console.log("%s");
  return javaObj.getString()
}