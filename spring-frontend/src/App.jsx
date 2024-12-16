import { useState } from "react";
import reactLogo from "./assets/react.svg";
import viteLogo from "/vite.svg";
import "./App.css";
import VideoUpload from "./components/VideoUpload";
import { Toaster } from "react-hot-toast";
import VideoPlayer from "./components/VideoPlayer";
import { Button, TextInput } from "flowbite-react";

function App() {
  const [count, setCount] = useState(0);
  const [fieldValue, setFieldValue] = useState("");
  const [videoId, setVideoId] = useState(
    "56f81959-f5b9-47af-a0ef-996bf55c43be"
  );

  function playVideo(videoId) {
    setVideoId(videoId);
  }

  return (
    <>
      <Toaster />
      <div className="min-h-screen bg-gray-100 dark:bg-gray-800 flex flex-col items-center py-12 space-y-12">
        <h1 className="text-3xl font-extrabold text-gray-800 dark:text-gray-100">
          Video Streaming App
        </h1>

        <div className="flex flex-col lg:flex-row lg:space-x-8 w-full px-8 lg:px-16">
          <div className="lg:w-1/2 w-full bg-white dark:bg-gray-900 rounded-lg shadow-md p-6 space-y-4">
            <h2 className="text-xl font-semibold text-gray-700 dark:text-gray-200 text-center">
              Playing Video
            </h2>

            <div className="mt-4">
              <VideoPlayer
                src={`http://localhost:8080/api/v1/videos/${videoId}/master.m3u8`}
              />
            </div>
          </div>

          <div className="lg:w-1/2 w-full bg-white dark:bg-gray-900 rounded-lg shadow-md p-6">
            <VideoUpload />
          </div>
        </div>

        <div className="flex flex-col sm:flex-row space-y-4 sm:space-y-0 sm:space-x-4 w-full max-w-md px-4">
          <TextInput
            className="flex-1"
            value={fieldValue}
            onChange={(event) => setFieldValue(event.target.value)}
            placeholder="Enter video ID here"
            name="video_id_field"
          />
          <Button
            className="bg-blue-500 hover:bg-blue-600 text-white"
            onClick={() => setVideoId(fieldValue)}
          >
            Play
          </Button>
        </div>
      </div>
    </>
  );
}

export default App;
