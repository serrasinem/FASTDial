# FASTDial
FASTDial: Abstracting Dialogue Policies for Fast Development of Task Oriented Agents

We present a novel abstraction framework called FASTDial for designing task oriented dialogue agents, built on top of the OpenDial toolkit. This framework is meant to facilitate prototyping and development of dialogue systems from scratch also by non-tech savvy especially when limited training data is available. To this end, we use a generic and simple frame-slots data-structure with pre-defined dialogue policies that allows for fast design and implementation at the price of some flexibility reduction. Moreover, it allows for minimizing programming effort and domain expert training time, by hiding away many implementation details. We provide a system demonstration screencast video in the following link: https://vimeo.com/329840716

The dialogue management service of FASTDial heavily  depends on the middleware interaction to retrieve the relevant information throughout the dialogue.  The necessary middleware interaction is handled by using web service responses/Telegram midware KB simulation. FASTDial produces 2-types of DMS outputs: i) Machine Utterance and ii) API call. 

You can find he further details in the paper listed below.

## Reference
If you are using FASTDial for research purposes, please cite:

Tekiroglu, S. S., Magnini, B., & Guerini, M. (2019, July). FASTDial: Abstracting Dialogue Policies for Fast Development of Task Oriented Agents. In Proceedings of the 57th Conference of the Association for Computational Linguistics: System Demonstrations (pp. 75-80).


FASTDial has been originally developed by the [Human Language Technology Natural Language Processing Group, HLT-NLP] (https://hlt-nlp.fbk.eu/) of Fondazione Bruno Kessler (Italy), with [Serra Sinem Tekiroglu, tekiroglu @ fbk . eu]  as the main developer. This service is a part of the Conversational Banking Frontend Project 
( https://ict.fbk.eu/projects/detail/conversational-banking-front-end/ ) funded by EIT Digital.