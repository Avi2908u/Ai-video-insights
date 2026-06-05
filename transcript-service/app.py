from flask import Flask, jsonify
from youtube_transcript_api import YouTubeTranscriptApi

app= Flask(__name__)

@app.route('/transcript/<video_id>')
def get_transcript(video_id):
    try:
        api=YouTubeTranscriptApi()

        transcript = api.fetch(video_id, languages=['en','hi'])

        full_text = " ".join(
            snippet.text
            for snippet in transcript
        )

        return jsonify({
            "videoId": video_id,
            "language": getattr(transcript, "language", "unknown"),
            "transcript": full_text
        })
    except Exception as e:
        return jsonify({"videoId": video_id, "error":str(e)}),500    

if __name__ == '__main__':
    app.run(port=5000)