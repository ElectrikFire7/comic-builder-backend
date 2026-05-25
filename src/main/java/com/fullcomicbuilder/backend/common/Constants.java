package com.fullcomicbuilder.backend.common;

public class Constants {

    public static final String CHARACTER_GENERATION_INSTRUCTION = """
        Your task is to generate a single, full-body character image
        based on the user's description and any reference images provided.
        
        Guidelines:
        - Maintain a consistent comic book illustration style.
        - Show the character from head to toe with clear facial features.
        - Apply the physical traits described (age, height, eye colour, hair, clothing, accessories).
        - If reference images are provided, match their art style and maintain character consistency.
        - If Current Image is provided, it means the user wants some changes on top of the previously generated image.
        - Output ONLY the generated image — no text, no explanation.
        """;

    public static final String LOCATION_GENERATION_INSTRUCTIONS = """
        Your task is to generate a single, detailed location or background image
        based on the user's description and any reference images provided.
        
        Guidelines:
        - Create environments with a strong sense of depth and spatial composition.
        - Use atmospheric lighting appropriate to the described mood or timing (e.g. dramatic, serene, mysterious, noon evening).
        - Convey environmental storytelling — the setting should feel like it belongs in a comic world.
        - Match the art style of Style Image if provided
        - If Sketch Template is provided, it might be detailed or outlined. Make the objects in the image accordingly. 
        - If Current Image is provided, it means the user wants some changes on top of the previously generated image.
        - Do NOT include any characters — backgrounds only.
        - Output ONLY the generated image — no text, no explanation.
        """;

    public static final String SCENE_GENERATION_INSTRUCTIONS = """
            Your sole task is to generate a single comic panel image depicting
            the described scene, based on the user's description and any reference images.
            
            Guidelines:
            - Compose the scene as a single comic panel with described visual narrative.
            - Apply cinematic framing — consider angle (close-up, wide shot, bird's eye) to best tell the story.
            - Convey action, emotion, and narrative tension through body language and composition.
            - If Sketch Template is provided, it might be detailed or outlined. Make the objects in the image accordingly.
            - Position characters meaningfully relative to each other and the environment.
            - Match the art style of Style Image if provided
            - If character images are provided, maintain style and character consistency and generate them where mentioned.
            - If Location image is provided. Use it as the background. Generate it with consistency.
            - Integrate backgrounds and characters cohesively in the same art style.
            - If Current Image is provided, it means the user wants some changes on top of the previously generated image.
            - Output ONLY the generated image — no text, no explanation.
            - Output image should not have any text or panel borders
            """;

    public static String[] categories = {
            "HARM_CATEGORY_HATE_SPEECH",
            "HARM_CATEGORY_SEXUALLY_EXPLICIT",
            "HARM_CATEGORY_DANGEROUS_CONTENT",
            "HARM_CATEGORY_HARASSMENT"
    };

}
