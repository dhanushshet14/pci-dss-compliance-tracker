from flask import Flask
from routes.categorize import categorize_bp

app = Flask(__name__)

app.register_blueprint(categorize_bp)

if __name__ == "__main__":
    app.run(debug=True)