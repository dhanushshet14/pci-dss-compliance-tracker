# ============================================================================
# PCI-DSS Compliance Tracker — AI Service (Flask Entry Point)
# ============================================================================

from flask import Flask

app = Flask(__name__)

# Register route blueprints
# from routes.describe import describe_bp
# from routes.recommend import recommend_bp
# app.register_blueprint(describe_bp)
# app.register_blueprint(recommend_bp)

@app.route("/health")
def health():
    return {"status": "ok"}

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
